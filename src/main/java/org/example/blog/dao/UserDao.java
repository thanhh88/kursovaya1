package org.example.blog.dao;

import org.example.blog.model.User;

public interface UserDao {

    boolean save(User user);

    User findById(Long id);

    User findByUsername(String username);

    boolean update(User user);
}
