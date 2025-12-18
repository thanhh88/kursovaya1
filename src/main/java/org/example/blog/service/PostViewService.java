package org.example.blog.service;

import org.example.blog.dao.PostViewDao;
import org.example.blog.dao.PostViewDaoImpl;
import org.example.blog.model.Post;
import org.example.blog.model.PostView;
import org.example.blog.model.User;

public class PostViewService {

    private final PostViewDao postViewDao = new PostViewDaoImpl();

    public void logView(Post post, User user) {
        if (post == null || post.getId() == null || user == null || user.getId() == null) {
            return;
        }

        PostView view = new PostView();
        view.setPost(post);
        view.setUser(user);

        postViewDao.save(view);
    }
}
