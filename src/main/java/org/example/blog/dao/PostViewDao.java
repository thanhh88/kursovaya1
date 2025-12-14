package org.example.blog.dao;

import org.example.blog.model.PostView;
import org.example.blog.model.User;

import java.time.LocalDate;
import java.util.List;

public interface PostViewDao {
    void save(PostView view);

    long countByUser(User user);

    List<Object[]> countDailyViews(User user, LocalDate from, LocalDate to);

    List<LocalDate> findDistinctViewDates(User user, LocalDate from, LocalDate to);

    List<Object[]> countByTopicForUser(User user);
}
