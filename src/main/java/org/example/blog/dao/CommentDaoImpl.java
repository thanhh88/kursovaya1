package org.example.blog.dao;

import org.example.blog.model.Comment;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import java.util.List;

public class CommentDaoImpl implements CommentDao {

    @Override
    public List<Comment> findByPostId(Long postId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Comment c " +
                                    "JOIN FETCH c.author " +
                                    "WHERE c.post.id = :postId " +
                                    "ORDER BY c.createdAt ASC",
                            Comment.class
                    )
                    .setParameter("postId", postId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
