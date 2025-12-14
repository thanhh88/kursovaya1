package org.example.blog.service;

import org.example.blog.model.User;

public interface UserService {

    boolean register(User user);

    User login(String username, String password);

    User findById(Long id);

    boolean updateProfile(User user);
}
