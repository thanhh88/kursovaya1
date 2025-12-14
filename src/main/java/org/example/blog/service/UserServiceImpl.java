package org.example.blog.service;

import org.example.blog.dao.UserDao;
import org.example.blog.model.User;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean register(User user) {
        if (user == null || user.getUsername() == null || user.getPasswordHash() == null) {
            return false;
        }
        // kiểm tra trùng username
        User existing = userDao.findByUsername(user.getUsername());
        if (existing != null) {
            return false;
        }
        // TODO: nếu muốn, sau này hash password
        return userDao.save(user);
    }

    @Override
    public User login(String username, String password) {
        if (username == null || password == null) return null;

        User user = userDao.findByUsername(username);
        if (user == null) return null;

        // tạm thời so sánh plain text
        if (password.equals(user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    @Override
    public User findById(Long id) {
        if (id == null) return null;
        return userDao.findById(id);
    }

    @Override
    public boolean updateProfile(User user) {
        if (user == null || user.getId() == null) {
            return false;
        }
        return userDao.update(user);
    }
}
