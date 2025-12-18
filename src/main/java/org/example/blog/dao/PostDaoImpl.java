package org.example.blog.dao;

import org.example.blog.model.Post;
import org.example.blog.model.PostStatus;
import org.example.blog.model.User;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

public class PostDaoImpl implements PostDAO {

    // ===== CRUD =====

    @Override
    public void save(Post post) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(post);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Post findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Post.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Post post) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(post);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Post post) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Post attached = em.find(Post.class, post.getId());
            if (attached != null) {
                em.remove(attached);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Post> findByAuthor(User author) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Post p " +
                                    "JOIN FETCH p.topic " +
                                    "WHERE p.author = :author " +
                                    "ORDER BY p.createdAt DESC",
                            Post.class
                    )
                    .setParameter("author", author)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // ===== Reader =====

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
            q.setParameter("status", PostStatus.PUBLISHED);
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
            q.setParameter("status", PostStatus.PUBLISHED);
            q.setParameter("kw", "%" + keyword + "%");
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    // ===== Statistics =====

    @Override
    public long countByAuthor(User author) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(p) FROM Post p WHERE p.author = :author",
                            Long.class
                    )
                    .setParameter("author", author)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public long countByAuthorAndStatus(User author, String status) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(p) FROM Post p " +
                                    "WHERE p.author = :author AND p.status = :status",
                            Long.class
                    )
                    .setParameter("author", author)
                    .setParameter("status", status)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public long sumViewsByAuthor(User author) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long result = em.createQuery(
                            "SELECT SUM(p.views) FROM Post p WHERE p.author = :author",
                            Long.class
                    )
                    .setParameter("author", author)
                    .getSingleResult();
            return result != null ? result : 0L;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> countByTopicForAuthor(User author) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT t.name, COUNT(p) " +
                                    "FROM Post p JOIN p.topic t " +
                                    "WHERE p.author = :author " +
                                    "GROUP BY t.name",
                            Object[].class
                    )
                    .setParameter("author", author)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
