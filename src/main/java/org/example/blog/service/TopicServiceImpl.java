package org.example.blog.service;

import org.example.blog.dao.TopicDAO;
import org.example.blog.model.Topic;

import java.util.List;

public class TopicServiceImpl implements TopicService {

    private final TopicDAO topicDAO = new TopicDAO();

    @Override
    public List<Topic> findAllTopics() {
        return topicDAO.findAll();
    }

    @Override
    public Topic findById(Long id) {
        return topicDAO.findById(id);
    }
}
