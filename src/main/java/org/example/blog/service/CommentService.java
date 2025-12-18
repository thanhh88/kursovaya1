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

            // Используйте управляемые сущности, чтобы избежать проблем с отсоединенными/временными объектами.
            Post managedPost = em.find(Post.class, post.getId());
            User managedAuthor = em.find(User.class, author.getId());

            if (managedPost == null) {
                throw new IllegalArgumentException("Post not found: " + post.getId());
            }
            if (managedAuthor == null) {
                throw new IllegalArgumentException("Author not found: " + author.getId());
            }

            Comment c = new Comment();
            c.setPost(managedPost);
            c.setAuthor(managedAuthor);
            c.setContent(content.trim());
            c.setCreatedAt(LocalDateTime.now());

            em.persist(c);

            // Увеличьте количество комментариев к публикации.
            Integer cnt = managedPost.getCommentsCount();
            if (cnt == null) cnt = 0;
            managedPost.setCommentsCount(cnt + 1);
            em.merge(managedPost);

            tx.commit();
            post.setCommentsCount(managedPost.getCommentsCount());

            return c;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
