package org.example.blog.dao;

import org.example.blog.model.Post;
import org.example.blog.model.User;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Implementation của PostDAO – mọi truy vấn JPA cho Post nằm ở đây.
 */
public class PostDaoImpl implements PostDAO {

    // ========== Các hàm cho Reader ==========

    @Override
    public List<Post> findAllPublished() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Post> q = em.createQuery(
                    "SELECT p FROM Post p " +
                            "JOIN FETCH p.author " +
                            "JOIN FETCH p.topic " +
                            "WHERE p.status = :status " +
                            "ORDER BY p.createdAt DESC",
                    Post.class
            );
            q.setParameter("status", "PUBLISHED");
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Post> searchPublished(String keyword) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Post> q = em.createQuery(
                    "SELECT p FROM Post p " +
                            "JOIN FETCH p.author " +
                            "JOIN FETCH p.topic " +
                            "WHERE p.status = :status " +
                            "AND (LOWER(p.title) LIKE LOWER(:kw) " +
                            "     OR LOWER(p.content) LIKE LOWER(:kw)) " +
                            "ORDER BY p.createdAt DESC",
                    Post.class
            );
            q.setParameter("status", "PUBLISHED");
            q.setParameter("kw", "%" + keyword + "%");
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    // ========== Các hàm thống kê cho Dashboard ==========

    @Override
    public long countByAuthor(User author) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(p) FROM Post p WHERE p.author = :author";
            TypedQuery<Long> q = em.createQuery(jpql, Long.class);
            q.setParameter("author", author);
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public long countByAuthorAndStatus(User author, String status) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(p) FROM Post p WHERE p.author = :author AND p.status = :status";
            TypedQuery<Long> q = em.createQuery(jpql, Long.class);
            q.setParameter("author", author);
            q.setParameter("status", status);
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public long sumViewsByAuthor(User author) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT SUM(p.views) FROM Post p WHERE p.author = :author";
            TypedQuery<Long> q = em.createQuery(jpql, Long.class);
            q.setParameter("author", author);
            Long result = q.getSingleResult();
            return result != null ? result : 0L;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> countByTopicForAuthor(User author) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql =
                    "SELECT t.name, COUNT(p) " +
                            "FROM Post p " +
                            "JOIN p.topic t " +
                            "WHERE p.author = :author " +
                            "GROUP BY t.name";
            TypedQuery<Object[]> q = em.createQuery(jpql, Object[].class);
            q.setParameter("author", author);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Post> findByAuthor(User author) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT p FROM Post p WHERE p.author = :author";
            TypedQuery<Post> q = em.createQuery(jpql, Post.class);
            q.setParameter("author", author);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    // Nếu trong PostDAO interface bạn thêm các hàm CRUD khác
    // (save, update, delete, findById...), hãy implement chúng ở đây.
}
