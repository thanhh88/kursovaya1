package org.example.blog.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.blog.model.Comment;
import org.example.blog.model.Post;
import org.example.blog.model.User;
import org.example.blog.service.CommentService;
import org.example.blog.service.PostService;
import org.example.blog.service.SavedPostService;
import org.example.blog.service.PostViewService;
import org.example.blog.session.Session;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class PostDetailController implements MainChildController {

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML private ImageView thumbnailImageView;

    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label topicLabel;
    @FXML private Label createdLabel;
    @FXML private Label viewLabel;

    @FXML private TextArea contentArea;

    @FXML private Button saveButton;
    @FXML private Label saveInfoLabel;

    @FXML private TableView<Comment> commentTable;
    @FXML private TableColumn<Comment, String> colCommentAuthor;
    @FXML private TableColumn<Comment, String> colCommentCreated;
    @FXML private TableColumn<Comment, String> colCommentContent;

    @FXML private TextArea newCommentArea;
    @FXML private Label commentMessageLabel;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PostService postService = new PostService();
    private final CommentService commentService = new CommentService();
    private final SavedPostService savedPostService = new SavedPostService();
    private final PostViewService postViewService = new PostViewService();

    private Post currentPost;

    @FXML
    private void initialize() {
        contentArea.setEditable(false);

        if (commentTable != null) {
            commentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            commentTable.setPlaceholder(new Label("Комментариев пока нет."));
        }

        colCommentAuthor.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue() != null && c.getValue().getAuthor() != null
                                ? safeStr(c.getValue().getAuthor().getFullName(), "(неизвестно)")
                                : "(неизвестно)"
                )
        );

        colCommentCreated.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue() != null && c.getValue().getCreatedAt() != null
                                ? c.getValue().getCreatedAt().format(formatter)
                                : ""
                )
        );

        colCommentContent.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue() != null ? safeStr(c.getValue().getContent(), "") : ""
                )
        );

        if (commentMessageLabel != null) commentMessageLabel.setText("");
        if (saveInfoLabel != null) saveInfoLabel.setText("");
    }

    public void setPost(Post post) {
        if (post == null) return;

        this.currentPost = post;

        // load image supports http/file/classpath
        loadThumbnailSmart(post.getThumbnailUrl());

        titleLabel.setText(safeStr(post.getTitle(), "(без названия)"));
        authorLabel.setText(post.getAuthor() != null
                ? safeStr(post.getAuthor().getFullName(), "(неизвестно)")
                : "(неизвестно)");
        topicLabel.setText(post.getTopic() != null
                ? safeStr(post.getTopic().getName(), "(без темы)")
                : "(без темы)");
        createdLabel.setText(post.getCreatedAt() != null
                ? post.getCreatedAt().format(formatter)
                : "");

        contentArea.setText(safeStr(post.getContent(), ""));

        int views = post.getViews() != null ? post.getViews() : 0;
        viewLabel.setText(String.valueOf(views));

        if (post.getId() != null) {
            try {
                postService.increaseViews(post.getId());
                viewLabel.setText(String.valueOf(views + 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logReaderView();
        updateSaveButtonState();
        loadComments();
    }

    private void loadThumbnailSmart(String path) {
        Image img = loadImageSmart(path);
        thumbnailImageView.setImage(img);
    }

    private Image loadImageSmart(String path) {
        if (path == null || path.isBlank()) return null;

        try {
            if (path.startsWith("http://") || path.startsWith("https://")) {
                return new Image(path, true);
            }

            if (path.startsWith("classpath:")) {
                String rel = path.substring("classpath:".length()); // "/images/..."
                URL url = getClass().getResource(rel);
                if (url != null) return new Image(url.toExternalForm(), true);
                return null;
            }

            if (path.startsWith("file:")) {
                return new Image(path, true);
            }

            return new Image("file:" + path, true);
        } catch (Exception e) {
            return null;
        }
    }

    private void logReaderView() {
        if (currentPost == null || currentPost.getId() == null) return;

        User user = Session.getCurrentUser();
        if (user == null || user.getId() == null) return;

        try {
            postViewService.logView(currentPost, user);
        } catch (Exception e) {
            // не падаем
        }
    }

    private void updateSaveButtonState() {
        if (currentPost == null || currentPost.getId() == null) {
            saveButton.setDisable(true);
            saveInfoLabel.setText("Запись недоступна для сохранения.");
            return;
        }

        User user = Session.getCurrentUser();
        if (user == null || user.getId() == null) {
            saveButton.setDisable(true);
            saveInfoLabel.setText("Войдите в систему, чтобы сохранить запись.");
            return;
        }

        boolean saved;
        try {
            saved = savedPostService.isSaved(currentPost, user);
        } catch (Exception e) {
            e.printStackTrace();
            saved = false;
        }

        saveButton.setDisable(false);
        if (saved) {
            saveButton.setText("Удалить из сохранённых");
            saveInfoLabel.setText("Запись сохранена.");
        } else {
            saveButton.setText("Сохранить");
            saveInfoLabel.setText("");
        }
    }

    private void loadComments() {
        if (currentPost == null || currentPost.getId() == null) {
            commentTable.setItems(FXCollections.observableArrayList(Collections.emptyList()));
            return;
        }

        try {
            List<Comment> list = commentService.getCommentsByPostId(currentPost.getId());
            commentTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            e.printStackTrace();
            commentTable.setItems(FXCollections.observableArrayList(Collections.emptyList()));
        }
    }

    @FXML
    private void handleAddComment() {
        commentMessageLabel.setText("");

        if (currentPost == null || currentPost.getId() == null) {
            commentMessageLabel.setText("Запись не найдена.");
            return;
        }

        User user = Session.getCurrentUser();
        if (user == null || user.getId() == null) {
            commentMessageLabel.setText("Войдите в систему, чтобы оставить комментарий.");
            return;
        }

        String text = newCommentArea.getText();
        if (text == null || text.isBlank()) {
            commentMessageLabel.setText("Комментарий не может быть пустым.");
            return;
        }

        try {
            commentService.addComment(currentPost, user, text.trim());
            newCommentArea.clear();
            commentMessageLabel.setText("Комментарий добавлен.");
            loadComments();
        } catch (Exception e) {
            e.printStackTrace();
            commentMessageLabel.setText("Ошибка при добавлении комментария.");
        }
    }

    @FXML
    private void handleToggleSave() {
        if (currentPost == null || currentPost.getId() == null) {
            saveInfoLabel.setText("Запись не найдена.");
            return;
        }

        User user = Session.getCurrentUser();
        if (user == null || user.getId() == null) {
            saveInfoLabel.setText("Войдите в систему.");
            return;
        }

        try {
            boolean saved = savedPostService.isSaved(currentPost, user);

            if (saved) {
                savedPostService.unsave(currentPost, user);
            } else {
                savedPostService.save(currentPost, user);
            }

            updateSaveButtonState();
        } catch (Exception e) {
            e.printStackTrace();
            saveInfoLabel.setText("Ошибка при сохранении записи.");
        }
    }

    @FXML
    private void handleBackToReader() {
        if (mainController != null) {
            mainController.openReaderPage();
        }
    }

    private String safeStr(String s, String fallback) {
        if (s == null) return fallback;
        String t = s.trim();
        return t.isEmpty() ? fallback : t;
    }
}
