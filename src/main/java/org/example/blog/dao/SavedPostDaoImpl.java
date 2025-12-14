package org.example.blog.dao;

import org.example.blog.model.Post;
import org.example.blog.model.SavedPost;
import org.example.blog.model.User;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

public class SavedPostDaoImpl implements SavedPostDao {

    @Override
    public boolean existsByUserAndPost(User user, Post post) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "select count(sp) from SavedPost sp " +
                                    "where sp.user = :user and sp.post = :post", Long.class)
                    .setParameter("user", user)
                    .setParameter("post", post)
                    .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    @Override
    public SavedPost findByUserAndPost(User user, Post post) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select sp from SavedPost sp " +
                                    "where sp.user = :user and sp.post = :post", SavedPost.class)
                    .setParameter("user", user)
                    .setParameter("post", post)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public void save(SavedPost savedPost) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(savedPost);

            // Post đã được setSavedCount() từ Service, merge lại để lưu
            em.merge(savedPost.getPost());

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(SavedPost savedPost) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            SavedPost attached = em.find(SavedPost.class, savedPost.getId());
            if (attached != null) {
                Post p = attached.getPost();
                em.remove(attached);

                // Post đã được setSavedCount() từ Service, merge lại để lưu
                em.merge(p);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Post> findSavedPostsByUser(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select sp.post from SavedPost sp " +
                                    "where sp.user = :user " +
                                    "order by sp.savedAt desc", Post.class)
                    .setParameter("user", user)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
