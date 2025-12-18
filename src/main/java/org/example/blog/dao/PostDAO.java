package org.example.blog.dao;

import org.example.blog.model.Post;
import org.example.blog.model.User;

import java.util.List;

public interface PostDAO {

    // CRUD cơ bản

    void save(Post post);

    Post findById(Long id);

    void update(Post post);

    void delete(Post post);

    List<Post> findByAuthor(User author);

    // ===== Reader mode =====
    List<Post> findAllPublished();

    List<Post> searchPublished(String keyword);

    // Dashboard / Statistics

    long countByAuthor(User author);

    long countByAuthorAndStatus(User author, String status);

    long sumViewsByAuthor(User author);
    List<Object[]> countByTopicForAuthor(User author);
}
