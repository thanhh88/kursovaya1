package org.example.blog.service;

import org.example.blog.model.Topic;

import java.util.List;

public interface TopicService {
    List<Topic> findAllTopics();
    Topic findById(Long id);
}
