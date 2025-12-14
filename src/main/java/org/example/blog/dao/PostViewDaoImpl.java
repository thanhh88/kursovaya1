package org.example.blog.dao;

import org.example.blog.model.PostView;
import org.example.blog.model.User;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class PostViewDaoImpl implements PostViewDao {

    @Override
    public void save(PostView view) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(view);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public long countByUser(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(v) FROM PostView v WHERE v.user = :user";
            TypedQuery<Long> q = em.createQuery(jpql, Long.class);
            q.setParameter("user", user);
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> countDailyViews(User user, LocalDate from, LocalDate to) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql =
                    "SELECT FUNCTION('date', v.viewedAt), COUNT(v) " +
                            "FROM PostView v " +
                            "WHERE v.user = :user " +
                            "AND v.viewedAt BETWEEN :from AND :to " +
                            "GROUP BY FUNCTION('date', v.viewedAt) " +
                            "ORDER BY FUNCTION('date', v.viewedAt)";
            TypedQuery<Object[]> q = em.createQuery(jpql, Object[].class);
            q.setParameter("user", user);
            q.setParameter("from", from.atStartOfDay());
            q.setParameter("to", LocalDateTime.of(to, LocalTime.MAX));
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<LocalDate> findDistinctViewDates(User user, LocalDate from, LocalDate to) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql =
                    "SELECT DISTINCT FUNCTION('date', v.viewedAt) " +
                            "FROM PostView v " +
                            "WHERE v.user = :user " +
                            "AND v.viewedAt BETWEEN :from AND :to " +
                            "ORDER BY FUNCTION('date', v.viewedAt)";
            TypedQuery<LocalDate> q = em.createQuery(jpql, LocalDate.class);
            q.setParameter("user", user);
            q.setParameter("from", from.atStartOfDay());
            q.setParameter("to", LocalDateTime.of(to, LocalTime.MAX));
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> countByTopicForUser(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql =
                    "SELECT t.name, COUNT(v) " +
                            "FROM PostView v " +
                            "JOIN v.post p " +
                            "JOIN p.topic t " +
                            "WHERE v.user = :user " +
                            "GROUP BY t.name";
            TypedQuery<Object[]> q = em.createQuery(jpql, Object[].class);
            q.setParameter("user", user);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
