package org.example.blog;

import org.example.blog.dao.UserDaoImpl;
import org.example.blog.model.*;
import org.example.blog.dao.*;
import org.example.blog.service.*;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;

public class JpaTestMain {
    public static void main(String[] args) {

        UserService userService = new UserServiceImpl(new UserDaoImpl());

        /*
         * ==========================================================
         * 1) TẠO TOPIC MẪU
         * ==========================================================
         */
        TopicDAO topicDAO = new TopicDAO();
        Topic tech = new Topic("Technology", "Công nghệ / AI / IT");
        Topic life = new Topic("Lifestyle", "Đời sống & phát triển bản thân");
        Topic travel = new Topic("Travel", "Du lịch & trải nghiệm");

        topicDAO.save(tech);
        topicDAO.save(life);
        topicDAO.save(travel);

        System.out.println("✔ Đã tạo 3 topic mẫu!");

        /*
         * ==========================================================
         * 2) TẠO USER MẪU LÀM TÁC GIẢ
         * ==========================================================
         */
        User author = new User();
        author.setUsername("author1");
        author.setPasswordHash("1234");
        author.setFullName("Author Demo");
        author.setAge(30);
        author.setCountry("Vietnam");
        author.setStatus("ACTIVE");

        userService.register(author);
        System.out.println("✔ Đã tạo user author!");

        /*
         * ==========================================================
         * 3) TẠO 3 BÀI VIẾT MẪU
         * ==========================================================
         */
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Post post1 = new Post();
            post1.setAuthor(author);
            post1.setTopic(tech);
            post1.setTitle("AI sẽ thay đổi thế giới như thế nào?");
            post1.setContent("Nội dung demo về AI...");
            post1.setStatus("PUBLISHED");
            post1.setViews(120);
            post1.setCommentsCount(5);
            post1.setSavedCount(3);
            post1.setCreatedAt(LocalDateTime.now().minusDays(2));

            Post post2 = new Post();
            post2.setAuthor(author);
            post2.setTopic(life);
            post2.setTitle("5 thói quen giúp bạn sống tích cực hơn");
            post2.setContent("Nội dung bài viết lifestyle demo...");
            post2.setStatus("PUBLISHED");
            post2.setViews(90);
            post2.setCommentsCount(2);
            post2.setSavedCount(4);
            post2.setCreatedAt(LocalDateTime.now().minusDays(1));

            Post post3 = new Post();
            post3.setAuthor(author);
            post3.setTopic(travel);
            post3.setTitle("Kinh nghiệm du lịch Đà Lạt tự túc");
            post3.setContent("Nội dung bài viết du lịch demo...");
            post3.setStatus("PUBLISHED");
            post3.setViews(200);
            post3.setCommentsCount(8);
            post3.setSavedCount(10);
            post3.setCreatedAt(LocalDateTime.now());

            em.persist(post1);
            em.persist(post2);
            em.persist(post3);

            tx.commit();

            System.out.println("✔ Đã tạo 3 bài viết mẫu!");
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            em.close();
        }


        /*
         * ==========================================================
         * 4) TEST REGISTER + LOGIN (mã gốc của bạn)
         * ==========================================================
         */
        User user = new User();
        user.setUsername("abc");
        user.setPasswordHash("1234");
        user.setFullName("Service Test");
        user.setAge(21);

        boolean ok = userService.register(user);
        System.out.println("Đăng ký: " + ok);

        User logged = userService.login("service_user", "1234");
        if (logged != null) {
            System.out.println("Đăng nhập thành công! Xin chào " + logged.getFullName());
        }

        System.out.println("🎉 DONE — bạn có thể mở JavaFX Reader mode để thấy bài viết!");
    }
}
