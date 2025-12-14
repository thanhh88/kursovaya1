package org.example.blog.service;

import org.example.blog.dao.SavedPostDao;
import org.example.blog.dao.SavedPostDaoImpl;
import org.example.blog.model.Post;
import org.example.blog.model.SavedPost;
import org.example.blog.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class SavedPostService {

    private final SavedPostDao savedPostDao;

    public SavedPostService() {
        this.savedPostDao = new SavedPostDaoImpl();
    }

    public SavedPostService(SavedPostDao savedPostDao) {
        this.savedPostDao = savedPostDao;
    }

    public boolean isSaved(Post post, User user) {
        if (post == null || user == null) return false;
        return savedPostDao.existsByUserAndPost(user, post);
    }

    public void save(Post post, User user) {
        if (post == null || user == null) return;
        if (savedPostDao.existsByUserAndPost(user, post)) {
            return; // đã lưu rồi
        }

        SavedPost sp = new SavedPost();
        sp.setPost(post);
        sp.setUser(user);
        sp.setSavedAt(LocalDateTime.now());

        // Cập nhật savedCount
        Integer savedCount = post.getSavedCount();
        if (savedCount == null) savedCount = 0;
        post.setSavedCount(savedCount + 1);

        savedPostDao.save(sp);
    }

    public void unsave(Post post, User user) {
        if (post == null || user == null) return;

        SavedPost sp = savedPostDao.findByUserAndPost(user, post);
        if (sp == null) return;

        Integer savedCount = post.getSavedCount();
        if (savedCount == null) savedCount = 0;
        if (savedCount > 0) savedCount--;
        post.setSavedCount(savedCount);

        savedPostDao.delete(sp);
    }

    public List<Post> getSavedPosts(User user) {
        if (user == null) return List.of();
        return savedPostDao.findSavedPostsByUser(user);
    }
}
