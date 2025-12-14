package org.example.blog.dao;

import org.example.blog.model.Topic;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public class TopicDAO {

    // Lưu topic mới
    public void save(Topic topic) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(topic);
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

    // Lấy tất cả topic (dùng cho màn Register chọn sở thích)
    public List<Topic> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT t FROM Topic t", Topic.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Tìm topic theo id nếu cần sau này
    public Topic findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Topic.class, id);
        } finally {
            em.close();
        }
    }
}
