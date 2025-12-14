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

import java.time.format.DateTimeFormatter;
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

        colCommentAuthor.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getAuthor() != null
                                ? c.getValue().getAuthor().getFullName()
                                : "(неизвестно)"
                ));

        colCommentCreated.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getCreatedAt() != null
                                ? c.getValue().getCreatedAt().format(formatter)
                                : ""
                ));

        colCommentContent.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getContent()));
    }

    public void setPost(Post post) {
        if (post == null) return;

        this.currentPost = post;

        loadThumbnail(post.getThumbnailUrl());

        titleLabel.setText(post.getTitle());
        authorLabel.setText(post.getAuthor() != null
                ? post.getAuthor().getFullName()
                : "(неизвестно)");
        topicLabel.setText(post.getTopic() != null
                ? post.getTopic().getName()
                : "(без темы)");
        createdLabel.setText(post.getCreatedAt() != null
                ? post.getCreatedAt().format(formatter)
                : "");
        contentArea.setText(post.getContent());

        int views = post.getViews() != null ? post.getViews() : 0;
        viewLabel.setText(String.valueOf(views));

        postService.increaseViews(post.getId());
        viewLabel.setText(String.valueOf(views + 1));

        logReaderView();
        updateSaveButtonState();
        loadComments();
    }

    private void loadThumbnail(String path) {
        if (path == null || path.isBlank()) {
            thumbnailImageView.setImage(null);
            return;
        }

        try {
            String url;
            if (path.startsWith("http")) url = path;
            else if (path.startsWith("file:")) url = path;
            else url = "file:" + path;

            thumbnailImageView.setImage(new Image(url, true));
        } catch (Exception e) {
            thumbnailImageView.setImage(null);
        }
    }

    private void logReaderView() {
        User user = Session.getCurrentUser();
        if (user == null) return;
        try {
            postViewService.logView(currentPost, user);
        } catch (Exception ignored) {}
    }

    private void updateSaveButtonState() {
        User user = Session.getCurrentUser();
        if (user == null) {
            saveButton.setDisable(true);
            saveInfoLabel.setText("Войдите в систему, чтобы сохранить запись.");
            return;
        }

        boolean saved = savedPostService.isSaved(currentPost, user);

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
        if (currentPost == null) return;

        List<Comment> list = commentService.getCommentsByPostId(currentPost.getId());
        commentTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleAddComment() {
        commentMessageLabel.setText("");

        User user = Session.getCurrentUser();
        if (user == null) {
            commentMessageLabel.setText("Войдите в систему, чтобы оставить комментарий.");
            return;
        }

        String text = newCommentArea.getText();
        if (text == null || text.isBlank()) {
            commentMessageLabel.setText("Комментарий не может быть пустым.");
            return;
        }

        commentService.addComment(currentPost, user, text.trim());
        newCommentArea.clear();
        commentMessageLabel.setText("Комментарий добавлен.");
        loadComments();
    }

    @FXML
    private void handleToggleSave() {
        User user = Session.getCurrentUser();
        if (user == null) {
            saveInfoLabel.setText("Войдите в систему.");
            return;
        }

        boolean saved = savedPostService.isSaved(currentPost, user);

        if (saved) savedPostService.unsave(currentPost, user);
        else savedPostService.save(currentPost, user);

        updateSaveButtonState();
    }

    @FXML
    private void handleBackToReader() {
        if (mainController != null) mainController.openReaderPage();
    }
}
