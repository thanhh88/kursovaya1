package org.example.blog.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.blog.model.Post;
import org.example.blog.model.PostStatus;
import org.example.blog.model.Topic;
import org.example.blog.model.User;
import org.example.blog.session.Session;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReaderController implements MainChildController {

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML private TextField searchField;
    @FXML private VBox postContainer;
    @FXML private ComboBox<String> sortBox;

    @FXML private ComboBox<Topic> topicFilterBox;
    @FXML private TextField authorFilterField;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TextField minViewsField;
    @FXML private TextField minCommentsField;
    @FXML private CheckBox onlySavedCheck;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private User currentUser;

    @FXML
    private void initialize() {
        currentUser = Session.getCurrentUser();
        loadTopicsForFilter();
        setupSortBox();
        loadPosts();
    }

    public void reloadPosts() {
        handleResetFilter();
    }

    private int safeInt(Integer v) {
        return v != null ? v : 0;
    }

    private void loadTopicsForFilter() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Topic> topics = em.createQuery(
                    "SELECT t FROM Topic t ORDER BY t.name", Topic.class
            ).getResultList();
            topicFilterBox.setItems(FXCollections.observableArrayList(topics));
        } catch (Exception e) {
            e.printStackTrace();
            topicFilterBox.setItems(FXCollections.observableArrayList());
        } finally {
            em.close();
        }
    }

    private void setupSortBox() {
        sortBox.setItems(FXCollections.observableArrayList(
                "Самые новые",
                "Наиболее просматриваемые",
                "Наиболее комментируемые"
        ));

        sortBox.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> refreshList());
    }

    private void loadPosts() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Post> query = em.createQuery(
                    "SELECT p FROM Post p " +
                            "JOIN FETCH p.author " +
                            "JOIN FETCH p.topic " +
                            "WHERE p.status = :status " +
                            "ORDER BY p.createdAt DESC",
                    Post.class
            );
            query.setParameter("status", PostStatus.PUBLISHED);

            List<Post> posts = query.getResultList();
            posts = applySortOrFavorite(posts);

            showPosts(posts);
        } catch (Exception e) {
            e.printStackTrace();
            showPosts(Collections.emptyList());
        } finally {
            em.close();
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = trim(searchField.getText());

        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT p FROM Post p " +
                            "JOIN FETCH p.author " +
                            "JOIN FETCH p.topic " +
                            "WHERE p.status = :status"
            );

            if (keyword != null) {
                jpql.append(" AND (LOWER(p.title) LIKE :kw OR LOWER(p.content) LIKE :kw)");
            }

            jpql.append(" ORDER BY p.createdAt DESC");

            TypedQuery<Post> query = em.createQuery(jpql.toString(), Post.class);
            query.setParameter("status", PostStatus.PUBLISHED);

            if (keyword != null) {
                query.setParameter("kw", "%" + keyword.toLowerCase() + "%");
            }

            List<Post> posts = query.getResultList();
            posts = applySortOrFavorite(posts);

            showPosts(posts);
        } catch (Exception e) {
            e.printStackTrace();
            showPosts(Collections.emptyList());
        } finally {
            em.close();
        }
    }

    @FXML
    private void handleAdvancedFilter() {
        String keyword = trim(searchField.getText());
        Topic selectedTopic = topicFilterBox.getValue();
        String authorName = trim(authorFilterField.getText());
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        Integer minViews = parseInt(minViewsField.getText());
        Integer minComments = parseInt(minCommentsField.getText());
        boolean onlySaved = onlySavedCheck.isSelected();

        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.plusDays(1).atStartOfDay() : null;

        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT DISTINCT p FROM Post p " +
                            "JOIN FETCH p.author " +
                            "JOIN FETCH p.topic "
            );

            Map<String, Object> params = new HashMap<>();

            if (onlySaved && currentUser != null && currentUser.getId() != null) {
                jpql.append(" JOIN p.savedByUsers sp ");
            }

            jpql.append(" WHERE p.status = :status ");
            params.put("status", PostStatus.PUBLISHED);

            if (keyword != null) {
                jpql.append(" AND (LOWER(p.title) LIKE :kw OR LOWER(p.content) LIKE :kw) ");
                params.put("kw", "%" + keyword.toLowerCase() + "%");
            }

            if (selectedTopic != null && selectedTopic.getId() != null) {
                jpql.append(" AND p.topic.id = :topicId ");
                params.put("topicId", selectedTopic.getId());
            }

            if (authorName != null) {
                jpql.append(" AND LOWER(p.author.fullName) LIKE :authorName ");
                params.put("authorName", "%" + authorName.toLowerCase() + "%");
            }

            if (fromDateTime != null) {
                jpql.append(" AND p.createdAt >= :fromDate ");
                params.put("fromDate", fromDateTime);
            }

            if (toDateTime != null) {
                jpql.append(" AND p.createdAt < :toDate ");
                params.put("toDate", toDateTime);
            }

            if (minViews != null) {
                jpql.append(" AND p.views >= :minViews ");
                params.put("minViews", minViews);
            }

            if (minComments != null) {
                jpql.append(" AND p.commentsCount >= :minComments ");
                params.put("minComments", minComments);
            }

            if (onlySaved && currentUser != null && currentUser.getId() != null) {
                jpql.append(" AND sp.user.id = :uid ");
                params.put("uid", currentUser.getId());
            }

            jpql.append(" ORDER BY p.createdAt DESC ");

            TypedQuery<Post> query = em.createQuery(jpql.toString(), Post.class);
            params.forEach(query::setParameter);

            List<Post> posts = query.getResultList();
            posts = applySortOrFavorite(posts);

            showPosts(posts);

        } catch (Exception e) {
            e.printStackTrace();
            showPosts(Collections.emptyList());
        } finally {
            em.close();
        }
    }

    private List<Post> applySortOrFavorite(List<Post> posts) {
        if (posts == null || posts.size() <= 1) return posts;

        if (sortBox.getSelectionModel() != null && !sortBox.getSelectionModel().isEmpty()) {
            return applySort(posts);
        }
        return sortByFavorite(posts);
    }

    private List<Post> applySort(List<Post> posts) {
        List<Post> sorted = new ArrayList<>(posts);
        String mode = sortBox.getSelectionModel().getSelectedItem();
        if (mode == null) return sorted;

        switch (mode) {
            case "Наиболее просматриваемые":
                sorted.sort((a, b) -> safeInt(b.getViews()) - safeInt(a.getViews()));
                break;
            case "Наиболее комментируемые":
                sorted.sort((a, b) -> safeInt(b.getCommentsCount()) - safeInt(a.getCommentsCount()));
                break;
            default:
                sorted.sort((a, b) -> {
                    LocalDateTime d1 = a.getCreatedAt();
                    LocalDateTime d2 = b.getCreatedAt();
                    if (d1 == null) return 1;
                    if (d2 == null) return -1;
                    return d2.compareTo(d1);
                });
        }
        return sorted;
    }

    private List<Post> sortByFavorite(List<Post> posts) {
        if (currentUser == null || currentUser.getFavoriteTopics() == null) return posts;

        Set<Long> favIds = new HashSet<>();
        currentUser.getFavoriteTopics().forEach(t -> {
            if (t.getId() != null) favIds.add(t.getId());
        });

        if (favIds.isEmpty()) return posts;

        List<Post> fav = new ArrayList<>();
        List<Post> other = new ArrayList<>();

        for (Post p : posts) {
            if (p.getTopic() != null && p.getTopic().getId() != null && favIds.contains(p.getTopic().getId())) {
                fav.add(p);
            } else {
                other.add(p);
            }
        }

        fav.addAll(other);
        return fav;
    }

    private void showPosts(List<Post> posts) {
        postContainer.getChildren().clear();

        if (posts == null || posts.isEmpty()) {
            Label empty = new Label("Нет записей для отображения.");
            empty.getStyleClass().add("text-secondary");
            postContainer.getChildren().add(empty);
            return;
        }

        for (Post p : posts) {
            postContainer.getChildren().add(createPostCard(p));
        }
    }

    private Node createPostCard(Post post) {
        HBox root = new HBox(12);
        root.getStyleClass().addAll("card", "post-card");

        ImageView thumb = new ImageView();
        thumb.getStyleClass().add("post-thumbnail");
        thumb.setFitWidth(140);
        thumb.setFitHeight(90);
        thumb.setPreserveRatio(true);

        // load image supports http/file/classpath
        setImageSmart(thumb, post.getThumbnailUrl());

        VBox textBox = new VBox(4);

        String title = post.getTitle() != null ? post.getTitle() : "(без названия)";
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("post-title");
        titleLabel.setWrapText(true);

        String authorName = (post.getAuthor() != null && post.getAuthor().getFullName() != null)
                ? post.getAuthor().getFullName()
                : "(нет автора)";
        String topicName = (post.getTopic() != null && post.getTopic().getName() != null)
                ? post.getTopic().getName()
                : "(без темы)";

        String metaText = authorName + " | " + topicName + " | " + safeInt(post.getViews()) + " просмотров";
        Label metaLabel = new Label(metaText);
        metaLabel.getStyleClass().add("post-meta");

        String dateText = post.getCreatedAt() != null ? post.getCreatedAt().format(dateFormatter) : "";
        Label dateLabel = new Label(dateText);
        dateLabel.getStyleClass().add("post-date");

        textBox.getChildren().addAll(titleLabel, metaLabel, dateLabel);

        root.getChildren().addAll(thumb, textBox);

        root.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openPostDetail(post);
            }
        });

        return root;
    }

    // ====== IMAGE HELPERS ======

    private void setImageSmart(ImageView view, String path) {
        Image img = loadImageSmart(path);
        view.setImage(img);
    }

    private Image loadImageSmart(String path) {
        if (path == null || path.isBlank()) return null;

        try {
            // 1) http/https
            if (path.startsWith("http://") || path.startsWith("https://")) {
                return new Image(path, true);
            }

            // 2) classpath:/...
            if (path.startsWith("classpath:")) {
                String rel = path.substring("classpath:".length()); // "/images/..."
                URL url = getClass().getResource(rel);
                if (url != null) {
                    return new Image(url.toExternalForm(), true);
                }
                return null;
            }

            // 3) file:...
            if (path.startsWith("file:")) {
                return new Image(path, true);
            }

            // 4) plain local path
            return new Image("file:" + path, true);

        } catch (Exception e) {
            return null;
        }
    }

    private void refreshList() {
        boolean hasAdvancedFilter =
                topicFilterBox.getValue() != null ||
                        trim(authorFilterField.getText()) != null ||
                        fromDatePicker.getValue() != null ||
                        toDatePicker.getValue() != null ||
                        trim(minViewsField.getText()) != null ||
                        trim(minCommentsField.getText()) != null ||
                        onlySavedCheck.isSelected();

        if (hasAdvancedFilter) {
            handleAdvancedFilter();
            return;
        }

        if (trim(searchField.getText()) != null) {
            handleSearch();
            return;
        }

        loadPosts();
    }

    private String trim(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private Integer parseInt(String s) {
        try {
            return (s == null || s.trim().isEmpty()) ? null : Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void openPostDetail(Post post) {
        if (mainController != null) {
            mainController.openPostDetailPage(post);
        }
    }

    @FXML
    private void handleResetFilter() {
        searchField.clear();
        topicFilterBox.setValue(null);
        authorFilterField.clear();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        minViewsField.clear();
        minCommentsField.clear();
        onlySavedCheck.setSelected(false);

        sortBox.getSelectionModel().clearSelection();
        loadPosts();
    }
}
