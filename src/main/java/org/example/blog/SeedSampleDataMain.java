package org.example.blog;

import org.example.blog.model.Post;
import org.example.blog.model.PostStatus;
import org.example.blog.model.Topic;
import org.example.blog.model.User;
import org.example.blog.util.JpaUtil;
import org.example.blog.util.PasswordUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

public class SeedSampleDataMain {

    // ====== 15 thumbnails in resources ======
    // Put these files in: src/main/resources/images/thumbnails/
    private static final String[] THUMBS = new String[] {
            "classpath:/images/thumbnails/t01.jpg",
            "classpath:/images/thumbnails/t02.jpg",
            "classpath:/images/thumbnails/t03.jpg",
            "classpath:/images/thumbnails/t04.jpg",
            "classpath:/images/thumbnails/t05.jpg",
            "classpath:/images/thumbnails/t06.jpg",
            "classpath:/images/thumbnails/t07.jpg",
            "classpath:/images/thumbnails/t08.jpg",
            "classpath:/images/thumbnails/t09.jpg",
            "classpath:/images/thumbnails/t10.jpg",
            "classpath:/images/thumbnails/t11.jpg",
            "classpath:/images/thumbnails/t12.jpg",
            "classpath:/images/thumbnails/t13.jpg",
            "classpath:/images/thumbnails/t14.jpg",
            "classpath:/images/thumbnails/t15.jpg"
    };

    private static String thumb(int index0Based) {
        return THUMBS[index0Based % THUMBS.length];
    }

    public static void main(String[] args) {

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // ================== 1) TOPICS ==================
            Topic t1  = new Topic("Технологии и IT",
                    "Статьи о программировании, гаджетах, искусственном интеллекте и новых технологиях.");
            Topic t2  = new Topic("Путешествия и культура стран",
                    "Опыт поездок, советы туристам, заметки о традициях и особенностях разных стран.");
            Topic t3  = new Topic("Образование и саморазвитие",
                    "Учёба, языки, развитие навыков и личностный рост.");
            Topic t4  = new Topic("Наука и популярная наука",
                    "Интересные научные факты, открытия и сложные вещи простым языком.");
            Topic t5  = new Topic("Здоровье и спорт",
                    "ЗОЖ, тренировки, психология и баланс между работой и отдыхом.");
            Topic t6  = new Topic("Искусство и литература",
                    "Книги, фильмы, музыка, театр, анализ произведений и рекомендации.");
            Topic t7  = new Topic("Карьера и личные финансы",
                    "Поиск работы, карьера и управление личным бюджетом.");
            Topic t8  = new Topic("Общество и современные тренды",
                    "Социальные сети, тренды, цифровая культура.");
            Topic t9  = new Topic("Игры и гик-культура",
                    "Компьютерные игры, настолки, комиксы, аниме.");
            Topic t10 = new Topic("Личный блог и опыт автора",
                    "Личные заметки, истории из жизни и размышления.");

            em.persist(t1);  em.persist(t2);  em.persist(t3);  em.persist(t4);  em.persist(t5);
            em.persist(t6);  em.persist(t7);  em.persist(t8);  em.persist(t9);  em.persist(t10);

            // ================== 2) USERS (5 AUTHORS) ==================
            // IMPORTANT: hash password so UserServiceImpl.login() can verify it.
            String hashed1234 = PasswordUtil.hashPassword("1234");

            User u1 = new User();
            u1.setUsername("anna_blog");
            u1.setPasswordHash(hashed1234);
            u1.setFullName("Анна Петрова");
            u1.setAge(21);
            u1.setCountry("Россия");
            u1.setStatus("Студент");
            u1.setFavoriteTopics(new HashSet<>(Arrays.asList(t1, t3, t6)));
            em.persist(u1);

            User u2 = new User();
            u2.setUsername("dima_travel");
            u2.setPasswordHash(hashed1234);
            u2.setFullName("Дмитрий Нгуен");
            u2.setAge(23);
            u2.setCountry("Вьетнам");
            u2.setStatus("Работающий");
            u2.setFavoriteTopics(new HashSet<>(Arrays.asList(t2, t8, t10)));
            em.persist(u2);

            User u3 = new User();
            u3.setUsername("oleg_dev");
            u3.setPasswordHash(hashed1234);
            u3.setFullName("Олег Иванов");
            u3.setAge(25);
            u3.setCountry("Россия");
            u3.setStatus("Младший разработчик");
            u3.setFavoriteTopics(new HashSet<>(Arrays.asList(t1, t4, t9)));
            em.persist(u3);

            User u4 = new User();
            u4.setUsername("maria_art");
            u4.setPasswordHash(hashed1234);
            u4.setFullName("Мария Ким");
            u4.setAge(20);
            u4.setCountry("Корея");
            u4.setStatus("Студент");
            u4.setFavoriteTopics(new HashSet<>(Arrays.asList(t6, t5, t3)));
            em.persist(u4);

            User u5 = new User();
            u5.setUsername("sergey_life");
            u5.setPasswordHash(hashed1234);
            u5.setFullName("Сергей Лебедев");
            u5.setAge(28);
            u5.setCountry("Германия");
            u5.setStatus("Аналитик");
            u5.setFavoriteTopics(new HashSet<>(Arrays.asList(t7, t8, t10)));
            em.persist(u5);

            LocalDateTime now = LocalDateTime.now();

            // ================== 3) POSTS (20 posts, use only 15 thumbs) ==================
            int i = 0;

            // ------- Анна (u1) -------
            Post a1 = new Post();
            a1.setAuthor(u1);
            a1.setTopic(t1);
            a1.setTitle("Мой первый проект на JavaFX");
            a1.setContent("В этой записи я кратко рассказываю, как сделала настольное приложение на JavaFX "
                    + "с использованием Hibernate для работы с базой данных.");
            a1.setStatus(PostStatus.PUBLISHED);
            a1.setViews(48); a1.setCommentsCount(3); a1.setSavedCount(5);
            a1.setCreatedAt(now.minusDays(6));
            a1.setThumbnailUrl(thumb(i++));
            em.persist(a1);

            Post a2 = new Post();
            a2.setAuthor(u1);
            a2.setTopic(t3);
            a2.setTitle("Как вести конспекты так, чтобы их хотелось перечитывать");
            a2.setContent("Несколько простых приёмов оформления конспектов, которые помогают лучше запоминать материал.");
            a2.setStatus(PostStatus.PUBLISHED);
            a2.setViews(30); a2.setCommentsCount(1); a2.setSavedCount(4);
            a2.setCreatedAt(now.minusDays(4));
            a2.setThumbnailUrl(thumb(i++));
            em.persist(a2);

            Post a3 = new Post();
            a3.setAuthor(u1);
            a3.setTopic(t6);
            a3.setTitle("Пять книг, которые изменили мой взгляд на учёбу");
            a3.setContent("Краткие заметки о книгах по саморазвитию и учебе, которые помогли мне перестать откладывать.");
            a3.setStatus(PostStatus.PUBLISHED);
            a3.setViews(26); a3.setCommentsCount(2); a3.setSavedCount(6);
            a3.setCreatedAt(now.minusDays(2));
            a3.setThumbnailUrl(thumb(i++));
            em.persist(a3);

            Post a4 = new Post();
            a4.setAuthor(u1);
            a4.setTopic(t3);
            a4.setTitle("План на следующий семестр");
            a4.setContent("Черновик записи о том, какие цели я ставлю себе на следующий учебный семестр.");
            a4.setStatus(PostStatus.DRAFT);
            a4.setViews(3); a4.setCommentsCount(0); a4.setSavedCount(0);
            a4.setCreatedAt(now.minusDays(1));
            a4.setThumbnailUrl(thumb(i++));
            em.persist(a4);

            // ------- Дмитрий (u2) -------
            Post d1 = new Post();
            d1.setAuthor(u2);
            d1.setTopic(t2);
            d1.setTitle("Са Па и Далат: две разные стороны Вьетнама");
            d1.setContent("Немного фотографий и впечатлений от поездок в горы Са Па и прохладный город Далат.");
            d1.setStatus(PostStatus.PUBLISHED);
            d1.setViews(55); d1.setCommentsCount(4); d1.setSavedCount(7);
            d1.setCreatedAt(now.minusDays(10));
            d1.setThumbnailUrl(thumb(i++));
            em.persist(d1);

            Post d2 = new Post();
            d2.setAuthor(u2);
            d2.setTopic(t8);
            d2.setTitle("Почему нам так сложно отложить телефон");
            d2.setContent("Размышления о том, как социальные сети влияют на внимание и привычки.");
            d2.setStatus(PostStatus.PUBLISHED);
            d2.setViews(40); d2.setCommentsCount(2); d2.setSavedCount(5);
            d2.setCreatedAt(now.minusDays(7));
            d2.setThumbnailUrl(thumb(i++));
            em.persist(d2);

            Post d3 = new Post();
            d3.setAuthor(u2);
            d3.setTopic(t10);
            d3.setTitle("Почему я начал вести блог на русском языке");
            d3.setContent("Небольшая история о том, как изучение языка превратилось в привычку вести блог.");
            d3.setStatus(PostStatus.PUBLISHED);
            d3.setViews(33); d3.setCommentsCount(3); d3.setSavedCount(4);
            d3.setCreatedAt(now.minusDays(5));
            d3.setThumbnailUrl(thumb(i++));
            em.persist(d3);

            Post d4 = new Post();
            d4.setAuthor(u2);
            d4.setTopic(t2);
            d4.setTitle("Черновик: маршрут по Европе на две недели");
            d4.setContent("Черновой план путешествия по нескольким городам Европы.");
            d4.setStatus(PostStatus.DRAFT);
            d4.setViews(1); d4.setCommentsCount(0); d4.setSavedCount(0);
            d4.setCreatedAt(now.minusDays(2));
            d4.setThumbnailUrl(thumb(i++));
            em.persist(d4);

            // ------- Олег (u3) -------
            Post o1 = new Post();
            o1.setAuthor(u3);
            o1.setTopic(t1);
            o1.setTitle("Что такое REST API простыми словами");
            o1.setContent("Краткое объяснение принципов REST и того, как они используются в веб-разработке.");
            o1.setStatus(PostStatus.PUBLISHED);
            o1.setViews(70); o1.setCommentsCount(5); o1.setSavedCount(8);
            o1.setCreatedAt(now.minusDays(12));
            o1.setThumbnailUrl(thumb(i++));
            em.persist(o1);

            Post o2 = new Post();
            o2.setAuthor(u3);
            o2.setTopic(t4);
            o2.setTitle("Почему наука — это не только формулы");
            o2.setContent("О том, как научные идеи меняют повседневную жизнь, даже если мы этого не замечаем.");
            o2.setStatus(PostStatus.PUBLISHED);
            o2.setViews(29); o2.setCommentsCount(1); o2.setSavedCount(3);
            o2.setCreatedAt(now.minusDays(9));
            o2.setThumbnailUrl(thumb(i++));
            em.persist(o2);

            Post o3 = new Post();
            o3.setAuthor(u3);
            o3.setTopic(t9);
            o3.setTitle("Чему меня научила игра в командные шутеры");
            o3.setContent("Немного о командной работе, реакции и умении держать эмоции под контролем.");
            o3.setStatus(PostStatus.PUBLISHED);
            o3.setViews(61); o3.setCommentsCount(6); o3.setSavedCount(5);
            o3.setCreatedAt(now.minusDays(3));
            o3.setThumbnailUrl(thumb(i++));
            em.persist(o3);

            Post o4 = new Post();
            o4.setAuthor(u3);
            o4.setTopic(t1);
            o4.setTitle("Черновик: заметки по проекту на Spring Boot");
            o4.setContent("Набросок архитектуры будущего учебного проекта.");
            o4.setStatus(PostStatus.DRAFT);
            o4.setViews(2); o4.setCommentsCount(0); o4.setSavedCount(0);
            o4.setCreatedAt(now.minusDays(1));
            o4.setThumbnailUrl(thumb(i++));
            em.persist(o4);

            // ------- Мария (u4) -------
            Post m1 = new Post();
            m1.setAuthor(u4);
            m1.setTopic(t6);
            m1.setTitle("Три фильма, которые хочется пересматривать каждый год");
            m1.setContent("Короткие впечатления о фильмах, которые дают ощущение праздника и вдохновения.");
            m1.setStatus(PostStatus.PUBLISHED);
            m1.setViews(37); m1.setCommentsCount(3); m1.setSavedCount(4);
            m1.setCreatedAt(now.minusDays(8));
            m1.setThumbnailUrl(thumb(i++));
            em.persist(m1);

            Post m2 = new Post();
            m2.setAuthor(u4);
            m2.setTopic(t5);
            m2.setTitle("Как я научилась регулярно делать зарядку");
            m2.setContent("Простые правила, которые помогли превратить утреннюю гимнастику в привычку.");
            m2.setStatus(PostStatus.PUBLISHED);
            m2.setViews(42); m2.setCommentsCount(2); m2.setSavedCount(6);
            m2.setCreatedAt(now.minusDays(6));
            m2.setThumbnailUrl(thumb(i++));
            em.persist(m2);

            Post m3 = new Post();
            m3.setAuthor(u4);
            m3.setTopic(t3);
            m3.setTitle("Учеба за границей: стоит ли игра свеч?");
            m3.setContent("Личный опыт подготовки к обучению в другой стране и плюсы/минусы такого решения.");
            m3.setStatus(PostStatus.PUBLISHED);
            m3.setViews(28); m3.setCommentsCount(1); m3.setSavedCount(3);
            m3.setCreatedAt(now.minusDays(4));
            m3.setThumbnailUrl(thumb(i++));
            em.persist(m3);

            Post m4 = new Post();
            m4.setAuthor(u4);
            m4.setTopic(t6);
            m4.setTitle("Черновик: идеи для арт-проекта");
            m4.setContent("Черновые наброски концепции интерактивной выставки.");
            m4.setStatus(PostStatus.DRAFT);
            m4.setViews(1); m4.setCommentsCount(0); m4.setSavedCount(0);
            m4.setCreatedAt(now.minusDays(1));
            m4.setThumbnailUrl(thumb(i++));
            em.persist(m4);

            // ------- Сергей (u5) -------
            Post s1 = new Post();
            s1.setAuthor(u5);
            s1.setTopic(t7);
            s1.setTitle("Личный бюджет: с чего начать, если вы студент");
            s1.setContent("Несколько базовых принципов, которые помогают не уходить в минус каждый месяц.");
            s1.setStatus(PostStatus.PUBLISHED);
            s1.setViews(50); s1.setCommentsCount(4); s1.setSavedCount(7);
            s1.setCreatedAt(now.minusDays(11));
            s1.setThumbnailUrl(thumb(i++));
            em.persist(s1);

            Post s2 = new Post();
            s2.setAuthor(u5);
            s2.setTopic(t8);
            s2.setTitle("Стоит ли бояться искусственного интеллекта");
            s2.setContent("Краткий разбор популярных страхов и того, что происходит на самом деле.");
            s2.setStatus(PostStatus.PUBLISHED);
            s2.setViews(46); s2.setCommentsCount(5); s2.setSavedCount(6);
            s2.setCreatedAt(now.minusDays(9));
            s2.setThumbnailUrl(thumb(i++));
            em.persist(s2);

            Post s3 = new Post();
            s3.setAuthor(u5);
            s3.setTopic(t10);
            s3.setTitle("Что изменилось за год ведения блога");
            s3.setContent("Небольшие итоги: чему я научился, какие записи оказались самыми популярными.");
            s3.setStatus(PostStatus.PUBLISHED);
            s3.setViews(39); s3.setCommentsCount(3); s3.setSavedCount(5);
            s3.setCreatedAt(now.minusDays(3));
            s3.setThumbnailUrl(thumb(i++));
            em.persist(s3);

            Post s4 = new Post();
            s4.setAuthor(u5);
            s4.setTopic(t7);
            s4.setTitle("Черновик: идеи для курса по личным финансам");
            s4.setContent("Набросок структуры будущего мини-курса по управлению деньгами.");
            s4.setStatus(PostStatus.DRAFT);
            s4.setViews(2); s4.setCommentsCount(0); s4.setSavedCount(0);
            s4.setCreatedAt(now.minusDays(1));
            s4.setThumbnailUrl(thumb(i++));
            em.persist(s4);

            tx.commit();
            System.out.println("✔ Демо-данные созданы: 10 тем, 5 авторов, 20 записей. Thumbnails: 15 local classpath images.");

        } catch (Exception e) {
            e.printStackTrace();
            if (tx.isActive()) tx.rollback();
        } finally {
            em.close();
            JpaUtil.close();
        }
    }
}
