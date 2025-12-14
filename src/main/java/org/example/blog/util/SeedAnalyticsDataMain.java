package org.example.blog.util;

import org.example.blog.model.*;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

/**
 * Script tạo dữ liệu “ảo” cho dashboard:
 * - Sinh PostView theo ngày cho 30 ngày gần nhất
 * - Thêm SavedPost & Comment ngẫu nhiên
 *
 * CHẠY MỘT LẦN bằng cấu hình "Application" bình thường.
 */
public class SeedAnalyticsDataMain {

    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            // 1. Lấy 1 user để làm "owner" cho hành vi đọc (reader)
            //    Bạn có thể đổi username cho phù hợp DB của bạn.
            // Lấy user có ID lớn nhất (tức là user mới tạo gần nhất)
            TypedQuery<User> userQuery = em.createQuery(
                    "SELECT u FROM User u ORDER BY u.id DESC",
                    User.class
            );
            userQuery.setMaxResults(1);
            List<User> users = userQuery.getResultList();

            if (users.isEmpty()) {
                System.out.println("Không có user nào trong DB. Thoát.");
                return;
            }

            User reader = users.get(0);
            System.out.println("Seed cho user mới nhất: " + reader.getUsername());

            // 2. Lấy một list Post để làm dữ liệu
            List<Post> posts = em.createQuery(
                            "SELECT p FROM Post p WHERE p.status = :status",
                            Post.class
                    ).setParameter("status", "PUBLISHED")
                    .getResultList();

            if (posts.isEmpty()) {
                System.out.println("Không có Post PUBLISHED nào. Thoát.");
                return;
            }

            System.out.println("Có " + posts.size() + " bài để seed analytics.");

            // 3. Tạo view mỗi ngày trong 30 ngày gần nhất
            LocalDate today = LocalDate.now();
            LocalDate from = today.minusDays(29);

            for (LocalDate d = from; !d.isAfter(today); d = d.plusDays(1)) {
                // mỗi ngày random 1-6 lượt xem
                int viewsToday = RANDOM.nextInt(6); // 0..5
                for (int i = 0; i < viewsToday; i++) {
                    Post randomPost = posts.get(RANDOM.nextInt(posts.size()));

                    PostView view = new PostView();
                    view.setUser(reader);
                    view.setPost(randomPost);

                    // thời gian trong ngày random
                    int hour = RANDOM.nextInt(20) + 4; // 4h..23h
                    int minute = RANDOM.nextInt(60);
                    LocalDateTime dt = LocalDateTime.of(d, LocalTime.of(hour, minute));
                    view.setViewedAt(dt);

                    em.persist(view);

                    // đồng thời tăng views tổng trên Post để Blogger dashboard cũng có số
                    Integer old = randomPost.getViews();
                    if (old == null) old = 0;
                    randomPost.setViews(old + 1);
                    em.merge(randomPost);
                }
            }

            // 4. Random một ít SavedPost cho reader
            int savedCount = Math.min(posts.size(), 5);
            for (int i = 0; i < savedCount; i++) {
                Post p = posts.get(RANDOM.nextInt(posts.size()));

                // tránh lưu trùng
                Long count = em.createQuery(
                                "SELECT COUNT(sp) FROM SavedPost sp WHERE sp.user = :u AND sp.post = :p",
                                Long.class
                        ).setParameter("u", reader)
                        .setParameter("p", p)
                        .getSingleResult();

                if (count == 0) {
                    SavedPost sp = new SavedPost();
                    sp.setUser(reader);
                    sp.setPost(p);
                    sp.setSavedAt(LocalDateTime.now().minusDays(RANDOM.nextInt(10)));
                    em.persist(sp);
                }
            }

            // 5. Random một ít comment cho reader
            int commentCount = Math.min(posts.size(), 5);
            for (int i = 0; i < commentCount; i++) {
                Post p = posts.get(RANDOM.nextInt(posts.size()));

                Comment c = new Comment();
                c.setAuthor(reader);
                c.setPost(p);
                c.setContent("Auto-generated comment #" + (i + 1));
                c.setCreatedAt(LocalDateTime.now().minusDays(RANDOM.nextInt(10)));
                em.persist(c);
            }

            em.getTransaction().commit();
            System.out.println("✅ Seed analytics data DONE.");
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
