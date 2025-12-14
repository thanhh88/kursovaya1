package org.example.blog.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "saved_posts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"})
)
public class SavedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N SavedPost - 1 User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // N SavedPost - 1 Post
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt = LocalDateTime.now();

    public SavedPost() {
    }

    public SavedPost(User user, Post post) {
        this.user = user;
        this.post = post;
        this.savedAt = LocalDateTime.now();
    }

    // ===== Getter & Setter =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }
}
