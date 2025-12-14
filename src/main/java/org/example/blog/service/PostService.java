package org.example.blog.service;

import org.example.blog.model.Post;
import org.example.blog.model.PostStatus;
import org.example.blog.model.User;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public class PostService {

    // ================== READER MODE ==================

    public List<Post> getAllPublishedPosts() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Post p " +
                                    "JOIN FETCH p.author " +
                                    "JOIN FETCH p.topic " +
                                    "WHERE p.status = :status " +
                                    "ORDER BY p.createdAt DESC",
                            Post.class
                    )
                    .setParameter("status", PostStatus.PUBLISHED)   // <<< важный момент
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void increaseViews(Long postId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Post post = em.find(Post.class, postId);
            if (post != null) {
                Integer current = post.getViews();
                if (current == null) current = 0;
                post.setViews(current + 1);
                em.merge(post);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // ================== BLOGGER MODE ==================

    public List<Post> getPostsByAuthor(User author) {
        if (author == null) {
            return List.of();
        }
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

    public void savePost(Post post) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (post.getId() == null) {
                em.persist(post);
            } else {
                em.merge(post);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void deletePost(Post post) {
        if (post == null || post.getId() == null) {
            return;
        }
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Post managed = em.find(Post.class, post.getId());
            if (managed != null) {
                em.remove(managed);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
