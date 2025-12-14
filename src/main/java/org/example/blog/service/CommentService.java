package org.example.blog.service;

import org.example.blog.dao.CommentDao;
import org.example.blog.dao.CommentDaoImpl;
import org.example.blog.model.Comment;
import org.example.blog.model.Post;
import org.example.blog.model.User;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.List;

public class CommentService {

    private final CommentDao commentDao = new CommentDaoImpl();

    public List<Comment> getCommentsByPostId(Long postId) {
        if (postId == null) return List.of();
        return commentDao.findByPostId(postId);
    }

    public Comment addComment(Post post, User author, String content) {
        if (post == null || post.getId() == null) {
            throw new IllegalArgumentException("Post is null");
        }
        if (author == null || author.getId() == null) {
            throw new IllegalArgumentException("Author (current user) is null");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment content is empty");
        }

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Comment c = new Comment();
            c.setPost(post);
            c.setAuthor(author);
            c.setContent(content);
            c.setCreatedAt(LocalDateTime.now());

            em.persist(c);

            tx.commit();
            return c;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
