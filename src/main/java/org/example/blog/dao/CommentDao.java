package org.example.blog.dao;

import org.example.blog.model.Comment;

import java.util.List;

public interface CommentDao {

    List<Comment> findByPostId(Long postId);
}
