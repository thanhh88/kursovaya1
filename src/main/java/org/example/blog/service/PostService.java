package org.example.blog.service;

import org.example.blog.dao.PostDAO;
import org.example.blog.dao.PostDaoImpl;
import org.example.blog.model.Post;
import org.example.blog.model.User;

import java.util.List;

public class PostService {

    private final PostDAO postDAO;

    public PostService() {
        this.postDAO = new PostDaoImpl();
    }

    public PostService(PostDAO postDAO) {
        this.postDAO = postDAO;
    }

    // READER MODE

    public List<Post> getAllPublishedPosts() {
        return postDAO.findAllPublished();
    }

    public List<Post> searchPublished(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllPublishedPosts();
        }
        return postDAO.searchPublished(keyword.trim());
    }

    public void increaseViews(Long postId) {
        if (postId == null) return;

        Post post = postDAO.findById(postId);
        if (post == null) return;

        Integer current = post.getViews();
        if (current == null) current = 0;

        post.setViews(current + 1);
        postDAO.update(post);
    }

    // BLOGGER MODE

    public List<Post> getPostsByAuthor(User author) {
        if (author == null) return List.of();
        return postDAO.findByAuthor(author);
    }

    public void savePost(Post post) {
        if (post == null) return;

        if (post.getId() == null) {
            postDAO.save(post);
        } else {
            postDAO.update(post);
        }
    }

    public void deletePost(Post post) {
        if (post == null || post.getId() == null) return;
        postDAO.delete(post);
    }

    public Post findById(Long id) {
        if (id == null) return null;
        return postDAO.findById(id);
    }
}
