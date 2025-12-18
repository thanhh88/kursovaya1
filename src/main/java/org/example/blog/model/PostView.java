package org.example.blog.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_views")
public class PostView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Какой User просматривает
    @ManyToOne(optional = false)
    private User user;

    // какой пост
    @ManyToOne(optional = false)
    private Post post;

    @Column(nullable = false)
    private LocalDateTime viewedAt = LocalDateTime.now();

    //constructors

    public PostView() {
    }

    public PostView(User user, Post post) {
        this.user = user;
        this.post = post;
        this.viewedAt = LocalDateTime.now();
    }

    // getters / setters

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {      // <<--- QUAN TRỌNG
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {      // <<--- QUAN TRỌNG
        this.post = post;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
}
