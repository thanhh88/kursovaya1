package org.example.blog.dao;

import org.example.blog.model.Post;
import org.example.blog.model.PostView;
import org.example.blog.model.User;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PostViewDaoImpl implements PostViewDao {

    @Override
    public void save(PostView view) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // Attach entities để tránh lỗi transient/detached
            Long postId = view.getPost() != null ? view.getPost().getId() : null;
            Long userId = view.getUser() != null ? view.getUser().getId() : null;

            if (postId == null || userId == null) {
                tx.rollback();
                return;
            }

            Post managedPost = em.find(Post.class, postId);
            User managedUser = em.find(User.class, userId);

            if (managedPost == null || managedUser == null) {
                tx.rollback();
                return;
            }

            PostView managedView = new PostView();
            managedView.setPost(managedPost);
            managedView.setUser(managedUser);
            // viewedAt mặc định now trong entity

            em.persist(managedView);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public long countByUser(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(v) FROM PostView v WHERE v.user = :user",
                            Long.class
                    )
                    .setParameter("user", user)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> countDailyViews(User user, LocalDate from, LocalDate to) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // trả về Object[]{ LocalDateTime viewedAt, Long count } kiểu đơn giản
            // (Bạn đang xử lý group trong Java ở DashboardService, nên method này có thể không dùng.)
            TypedQuery<Object[]> q = em.createQuery(
                    "SELECT v.viewedAt, COUNT(v) " +
                            "FROM PostView v " +
                            "WHERE v.user = :user AND v.viewedAt BETWEEN :from AND :to " +
                            "GROUP BY v.viewedAt",
                    Object[].class
            );
            q.setParameter("user", user);
            q.setParameter("from", from.atStartOfDay());
            q.setParameter("to", to.plusDays(1).atStartOfDay());
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<LocalDate> findDistinctViewDates(User user, LocalDate from, LocalDate to) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<LocalDateTime> q = em.createQuery(
                    "SELECT v.viewedAt FROM PostView v " +
                            "WHERE v.user = :user AND v.viewedAt BETWEEN :from AND :to",
                    LocalDateTime.class
            );
            q.setParameter("user", user);
            q.setParameter("from", from.atStartOfDay());
            q.setParameter("to", to.plusDays(1).atStartOfDay());

            return q.getResultList().stream()
                    .map(LocalDateTime::toLocalDate)
                    .distinct()
                    .sorted()
                    .toList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> countByTopicForUser(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Object[]> q = em.createQuery(
                    "SELECT t.name, COUNT(v) " +
                            "FROM PostView v " +
                            "JOIN v.post p " +
                            "JOIN p.topic t " +
                            "WHERE v.user = :user " +
                            "GROUP BY t.name",
                    Object[].class
            );
            q.setParameter("user", user);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
