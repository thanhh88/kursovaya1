package org.example.blog.service;

import org.example.blog.dao.PostDAO;
import org.example.blog.dao.PostDaoImpl;
import org.example.blog.dao.SavedPostDao;
import org.example.blog.dao.SavedPostDaoImpl;
import org.example.blog.model.Post;
import org.example.blog.model.SavedPost;
import org.example.blog.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class SavedPostService {

    private final SavedPostDao savedPostDao;
    private final PostDAO postDAO;

    public SavedPostService() {
        this.savedPostDao = new SavedPostDaoImpl();
        this.postDAO = new PostDaoImpl();
    }

    public SavedPostService(SavedPostDao savedPostDao, PostDAO postDAO) {
        this.savedPostDao = savedPostDao;
        this.postDAO = postDAO;
    }

    public boolean isSaved(Post post, User user) {
        if (post == null || user == null) return false;
        if (post.getId() == null || user.getId() == null) return false;
        return savedPostDao.existsByUserAndPost(user, post);
    }

    public void save(Post post, User user) {
        if (post == null || user == null) return;
        if (post.getId() == null || user.getId() == null) return;

        // Для более стабильного увеличения значения savedCount, получите последний POST-запрос из базы данных.
        Post managedPost = postDAO.findById(post.getId());
        if (managedPost == null) return;

        if (savedPostDao.existsByUserAndPost(user, managedPost)) return;

        SavedPost sp = new SavedPost();
        sp.setPost(managedPost);
        sp.setUser(user);
        sp.setSavedAt(LocalDateTime.now());

        Integer savedCount = managedPost.getSavedCount();
        if (savedCount == null) savedCount = 0;
        managedPost.setSavedCount(savedCount + 1);

        // DAO persist SavedPost + merge Post
        savedPostDao.save(sp);
        post.setSavedCount(managedPost.getSavedCount());
    }

    public void unsave(Post post, User user) {
        if (post == null || user == null) return;
        if (post.getId() == null || user.getId() == null) return;

        Post managedPost = postDAO.findById(post.getId());
        if (managedPost == null) return;

        SavedPost sp = savedPostDao.findByUserAndPost(user, managedPost);
        if (sp == null) return;

        Integer savedCount = managedPost.getSavedCount();
        if (savedCount == null) savedCount = 0;
        if (savedCount > 0) savedCount--;
        managedPost.setSavedCount(savedCount);

        savedPostDao.delete(sp);

        // Повторно синхронизируйте POST-объект в пользовательском интерфейсе.
        post.setSavedCount(managedPost.getSavedCount());
    }

    public List<Post> getSavedPosts(User user) {
        if (user == null || user.getId() == null) return List.of();
        return savedPostDao.findSavedPostsByUser(user);
    }
}
