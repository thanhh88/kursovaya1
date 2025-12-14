package org.example.blog.dao;

import org.example.blog.model.Post;
import org.example.blog.model.SavedPost;
import org.example.blog.model.User;

import java.util.List;

public interface SavedPostDao {

    boolean existsByUserAndPost(User user, Post post);

    SavedPost findByUserAndPost(User user, Post post);

    void save(SavedPost savedPost);

    void delete(SavedPost savedPost);

    List<Post> findSavedPostsByUser(User user);
}
