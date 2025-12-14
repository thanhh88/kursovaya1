package org.example.blog.service;

import org.example.blog.dao.PostDAO;
import org.example.blog.dao.PostDaoImpl;
import org.example.blog.model.Post;
import org.example.blog.model.User;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Service cho Dashboard – dùng PostDAO cho phần Post,
 * Comment / SavedPost / PostView dùng JPA trực tiếp.
 */
public class DashboardService {

    // =============== DTOs dùng cho Controller ===============

    /** DTO: số bài theo topic (Blogger). */
    public static class TopicPostCount {
        private final String topicName;
        private final long count;

        public TopicPostCount(String topicName, long count) {
            this.topicName = topicName;
            this.count = count;
        }

        public String getTopicName() {
            return topicName;
        }

        public long getCount() {
            return count;
        }
    }

    /** DTO: thống kê sở thích đọc theo topic (Reader). */
    public static class ReaderTopicStat {
        private final String topicName;
        private long savedCount;
        private long commentedCount;
        private long viewedCount;

        public ReaderTopicStat(String topicName) {
            this.topicName = topicName;
        }

        public String getTopicName() {
            return topicName;
        }

        public long getSavedCount() {
            return savedCount;
        }

        public void setSavedCount(long savedCount) {
            this.savedCount = savedCount;
        }

        public long getCommentedCount() {
            return commentedCount;
        }

        public void setCommentedCount(long commentedCount) {
            this.commentedCount = commentedCount;
        }

        public long getViewedCount() {
            return viewedCount;
        }

        public void setViewedCount(long viewedCount) {
            this.viewedCount = viewedCount;
        }

        /** Điểm “yêu thích”: saved > comment > view. */
        public double getScore() {
            return savedCount * 3 + commentedCount * 2 + viewedCount;
        }
    }

    /** DTO: top bài viết với Engagement Score (Blogger). */
    public static class TopPostStat {
        private final Post post;
        private final long views;
        private final long comments;
        private final long saved;

        public TopPostStat(Post post, long views, long comments, long saved) {
            this.post = post;
            this.views = views;
            this.comments = comments;
            this.saved = saved;
        }

        public Post getPost() {
            return post;
        }

        public String getTitle() {
            return post != null ? post.getTitle() : "";
        }

        public long getViews() {
            return views;
        }

        public long getComments() {
            return comments;
        }

        public long getSaved() {
            return saved;
        }

        public double getEngagementScore() {
            return views * 0.5 + comments * 2 + saved * 3;
        }
    }

    // =============== DAO phụ thuộc ===============

    private final PostDAO postDao;

    public DashboardService() {
        this.postDao = new PostDaoImpl();
    }

    // Constructor cho unit test / DI nếu cần
    public DashboardService(PostDAO postDao) {
        this.postDao = postDao;
    }

    // =============== BLOGGER – THỐNG KÊ CHUNG ===============

    public long countTotalViews(User user) {
        return postDao.sumViewsByAuthor(user);
    }

    public long countPosts(User user) {
        return postDao.countByAuthor(user);
    }

    public long countPostsByStatus(User user, String status) {
        return postDao.countByAuthorAndStatus(user, status);
    }

    /** Số comment user đã viết. */
    public long countComments(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(c) FROM Comment c WHERE c.author = :author";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("author", user);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /** Số bài user đã lưu. */
    public long countSavedPosts(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(sp) FROM SavedPost sp WHERE sp.user = :user";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("user", user);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /** Đếm số bài theo topic cho Blogger. */
    public List<TopicPostCount> countPostsByTopic(User user) {
        List<Object[]> rows = postDao.countByTopicForAuthor(user);
        List<TopicPostCount> result = new ArrayList<>();
        for (Object[] row : rows) {
            String topicName = (String) row[0];
            Long count = (Long) row[1];
            result.add(new TopicPostCount(topicName, count));
        }
        return result;
    }

    // =============== BLOGGER – TOP POSTS (Engagement) ===============

    public List<TopPostStat> getTopPostsByEngagement(User author, int limit) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // 1) Lấy tất cả post của tác giả (qua PostDAO)
            List<Post> posts = postDao.findByAuthor(author);
            if (posts.isEmpty()) {
                return Collections.emptyList();
            }

            List<Long> postIds = new ArrayList<>();
            for (Post p : posts) {
                postIds.add(p.getId());   // cần Post có getId()
            }

            // 2) Đếm comment theo post
            Map<Long, Long> commentsMap = new HashMap<>();
            TypedQuery<Object[]> commentsQuery = em.createQuery(
                    "SELECT c.post.id, COUNT(c) " +
                            "FROM Comment c " +
                            "WHERE c.post.id IN :postIds " +
                            "GROUP BY c.post.id",
                    Object[].class);
            commentsQuery.setParameter("postIds", postIds);
            for (Object[] row : commentsQuery.getResultList()) {
                Long postId = (Long) row[0];
                Long count = (Long) row[1];
                commentsMap.put(postId, count);
            }

            // 3) Đếm saved theo post
            Map<Long, Long> savedMap = new HashMap<>();
            TypedQuery<Object[]> savedQuery = em.createQuery(
                    "SELECT sp.post.id, COUNT(sp) " +
                            "FROM SavedPost sp " +
                            "WHERE sp.post.id IN :postIds " +
                            "GROUP BY sp.post.id",
                    Object[].class);
            savedQuery.setParameter("postIds", postIds);
            for (Object[] row : savedQuery.getResultList()) {
                Long postId = (Long) row[0];
                Long count = (Long) row[1];
                savedMap.put(postId, count);
            }

            // 4) Gộp lại
            List<TopPostStat> stats = new ArrayList<>();
            for (Post p : posts) {
                long views = p.getViews(); // cần Post có field views + getter
                long comments = commentsMap.getOrDefault(p.getId(), 0L);
                long saved = savedMap.getOrDefault(p.getId(), 0L);
                stats.add(new TopPostStat(p, views, comments, saved));
            }

            stats.sort(Comparator.comparingDouble(TopPostStat::getEngagementScore).reversed());
            if (stats.size() > limit) {
                return new ArrayList<>(stats.subList(0, limit));
            }
            return stats;

        } finally {
            em.close();
        }
    }

    // =============== READER – THỐNG KÊ ===============

    /** Số post khác nhau mà user đã comment. */
    public long countCommentedPosts(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(DISTINCT c.post.id) FROM Comment c WHERE c.author = :author";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("author", user);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /** Tổng số lượt xem user đã thực hiện (PostView). */
    public long countViewedPostsByUser(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(v) FROM PostView v WHERE v.user = :user";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("user", user);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public Map<LocalDate, Long> getDailyViewsForUser(User user, LocalDate from, LocalDate to) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Lấy toàn bộ thời điểm xem, xử lý sang LocalDate trong Java
            String jpql =
                    "SELECT v.viewedAt " +
                            "FROM PostView v " +
                            "WHERE v.user = :user " +
                            "AND v.viewedAt BETWEEN :from AND :to";

            TypedQuery<java.time.LocalDateTime> query =
                    em.createQuery(jpql, java.time.LocalDateTime.class);
            query.setParameter("user", user);
            query.setParameter("from", from.atStartOfDay());
            query.setParameter("to", to.plusDays(1).atStartOfDay()); // đến hết ngày 'to'

            Map<LocalDate, Long> result = new java.util.LinkedHashMap<>();

            for (java.time.LocalDateTime dt : query.getResultList()) {
                LocalDate day = dt.toLocalDate();
                result.merge(day, 1L, Long::sum);
            }

            // Sắp xếp theo ngày tăng dần cho đẹp
            return result.entrySet().stream()
                    .sorted(java.util.Map.Entry.comparingByKey())
                    .collect(java.util.stream.Collectors.toMap(
                            java.util.Map.Entry::getKey,
                            java.util.Map.Entry::getValue,
                            (a, b) -> a,
                            java.util.LinkedHashMap::new
                    ));
        } finally {
            em.close();
        }
    }

    /** Top topics Reader yêu thích. */
    public List<ReaderTopicStat> getReaderTopicStats(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Map<String, ReaderTopicStat> map = new HashMap<>();

            // 1) SavedPost theo topic
            String jpqlSaved = "SELECT t.name, COUNT(sp) " +
                    "FROM SavedPost sp " +
                    "JOIN sp.post p " +
                    "JOIN p.topic t " +
                    "WHERE sp.user = :user " +
                    "GROUP BY t.name";
            TypedQuery<Object[]> qSaved = em.createQuery(jpqlSaved, Object[].class);
            qSaved.setParameter("user", user);
            for (Object[] row : qSaved.getResultList()) {
                String topicName = (String) row[0];
                Long count = (Long) row[1];
                ReaderTopicStat stat = map.computeIfAbsent(topicName, ReaderTopicStat::new);
                stat.setSavedCount(count);
            }

            // 2) Comment theo topic
            String jpqlComments = "SELECT t.name, COUNT(c) " +
                    "FROM Comment c " +
                    "JOIN c.post p " +
                    "JOIN p.topic t " +
                    "WHERE c.author = :author " +
                    "GROUP BY t.name";
            TypedQuery<Object[]> qComments = em.createQuery(jpqlComments, Object[].class);
            qComments.setParameter("author", user);
            for (Object[] row : qComments.getResultList()) {
                String topicName = (String) row[0];
                Long count = (Long) row[1];
                ReaderTopicStat stat = map.computeIfAbsent(topicName, ReaderTopicStat::new);
                stat.setCommentedCount(count);
            }

            // 3) PostView theo topic
            String jpqlViews = "SELECT t.name, COUNT(v) " +
                    "FROM PostView v " +
                    "JOIN v.post p " +
                    "JOIN p.topic t " +
                    "WHERE v.user = :user " +
                    "GROUP BY t.name";
            TypedQuery<Object[]> qViews = em.createQuery(jpqlViews, Object[].class);
            qViews.setParameter("user", user);
            for (Object[] row : qViews.getResultList()) {
                String topicName = (String) row[0];
                Long count = (Long) row[1];
                ReaderTopicStat stat = map.computeIfAbsent(topicName, ReaderTopicStat::new);
                stat.setViewedCount(count);
            }

            List<ReaderTopicStat> list = new ArrayList<>(map.values());
            list.sort(Comparator.comparingDouble(ReaderTopicStat::getScore).reversed());
            return list;

        } finally {
            em.close();
        }
    }

    public int getReadingStreak(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            LocalDate from = today.minusDays(60);

            String jpql =
                    "SELECT v.viewedAt " +
                            "FROM PostView v " +
                            "WHERE v.user = :user " +
                            "AND v.viewedAt BETWEEN :from AND :to";

            TypedQuery<java.time.LocalDateTime> query =
                    em.createQuery(jpql, java.time.LocalDateTime.class);
            query.setParameter("user", user);
            query.setParameter("from", from.atStartOfDay());
            query.setParameter("to", today.plusDays(1).atStartOfDay());

            java.util.Set<LocalDate> dates = new java.util.HashSet<>();
            for (java.time.LocalDateTime dt : query.getResultList()) {
                dates.add(dt.toLocalDate());
            }

            // Đếm streak từ hôm nay lùi lại
            int streak = 0;
            LocalDate current = today;
            while (dates.contains(current)) {
                streak++;
                current = current.minusDays(1);
            }
            return streak;

        } finally {
            em.close();
        }
    }
}
