package org.example.blog;

import org.example.blog.dao.UserDaoImpl;
import org.example.blog.service.UserService;
import org.example.blog.service.UserServiceImpl;

public class AppContext {

    private static final UserService userService = new UserServiceImpl(new UserDaoImpl());

    public static UserService getUserService() {
        return userService;
    }
}
