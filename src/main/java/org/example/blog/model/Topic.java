package org.example.blog.model;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "topics")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 1 Topic - N Posts
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Post> posts;

    // N - N với User
    @ManyToMany(mappedBy = "favoriteTopics")
    private Set<User> users = new HashSet<>();

    // Constructors

    public Topic() {
    }
    // Constructor
    public Topic(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
