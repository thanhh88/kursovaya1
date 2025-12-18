package org.example.blog.service;

import org.example.blog.dao.UserDao;
import org.example.blog.model.User;
import org.example.blog.util.PasswordUtil;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean register(User user) {
        if (user == null) return false;

        String username = user.getUsername();
        String rawPassword = user.getPasswordHash(); // hiện UI đang set vào passwordHash (raw). Giữ logic, nhưng sẽ hash lại.

        if (username == null || username.isBlank()) return false;
        if (rawPassword == null || rawPassword.isBlank()) return false;

        // Проверьте наличие повторяющихся имен пользователей.
        User existing = userDao.findByUsername(username.trim());
        if (existing != null) return false;

        // HASH password
        user.setUsername(username.trim());
        user.setPasswordHash(PasswordUtil.hashPassword(rawPassword));

        return userDao.save(user);
    }

    @Override
    public User login(String username, String password) {
        if (username == null || password == null) return null;

        User user = userDao.findByUsername(username.trim());
        if (user == null) return null;

        String stored = user.getPasswordHash();
        if (stored == null) return null;
        if (PasswordUtil.verifyPassword(password, stored)) {
            return user;
        }
        if (password.equals(stored)) {
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
        if (user == null || user.getId() == null) return false;
        return userDao.update(user);
    }
}
