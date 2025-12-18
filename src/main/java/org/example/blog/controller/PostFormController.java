package org.example.blog.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.blog.model.Post;
import org.example.blog.model.PostStatus;
import org.example.blog.model.Topic;
import org.example.blog.model.User;
import org.example.blog.service.PostService;
import org.example.blog.session.Session;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import java.io.File;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class PostFormController implements MainChildController {

    @FXML private TextField titleField;
    @FXML private ComboBox<Topic> topicCombo;
    @FXML private ComboBox<String> statusCombo;   // "Черновик" / "Опубликовано"
    @FXML private TextArea contentArea;
    @FXML private Button saveButton;

    @FXML private Label thumbnailFileLabel;
    @FXML private ImageView thumbnailPreview;

    private final PostService postService = new PostService();

    private Post editingPost;
    private Runnable onSaveCallback;
    private MainController mainController;

    private String selectedThumbnailPath;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        statusCombo.getItems().setAll(
                PostStatus.DRAFT,
                PostStatus.PUBLISHED
        );
        loadTopicsFromDb();

        if (thumbnailFileLabel != null) {
            thumbnailFileLabel.setText("Нет выбранного файла");
        }
    }

    private void loadTopicsFromDb() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Topic> topics = em.createQuery(
                    "SELECT t FROM Topic t ORDER BY t.name", Topic.class
            ).getResultList();
            topicCombo.getItems().setAll(topics);
        } catch (Exception e) {
            e.printStackTrace();
            topicCombo.getItems().setAll(Collections.emptyList());
        } finally {
            em.close();
        }
    }

    public void setEditingPost(Post post) {
        this.editingPost = post;

        if (post != null) {
            titleField.setText(safeStr(post.getTitle()));
            contentArea.setText(safeStr(post.getContent()));

            if (post.getTopic() != null) {
                topicCombo.getSelectionModel().select(post.getTopic());
            } else {
                topicCombo.getSelectionModel().clearSelection();
            }

            if (post.getStatus() != null) {
                statusCombo.getSelectionModel().select(post.getStatus());
            } else {
                statusCombo.getSelectionModel().select(PostStatus.DRAFT);
            }

            selectedThumbnailPath = post.getThumbnailUrl();
            updateThumbnailUI(selectedThumbnailPath);

            if (post.getCreatedAt() == null) {
                post.setCreatedAt(LocalDateTime.now()); // safety
            }
        } else {
            statusCombo.getSelectionModel().select(PostStatus.DRAFT);
            topicCombo.getSelectionModel().clearSelection();
            selectedThumbnailPath = null;
            updateThumbnailUI(null);
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void handleChooseThumbnail() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите изображение превью");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = chooser.showOpenDialog(thumbnailPreview.getScene().getWindow());
        if (file == null) return;

        try {
            Path destDir = Paths.get("images", "thumbnails");
            Files.createDirectories(destDir);

            String fileName = System.currentTimeMillis() + "_" + file.getName();
            Path dest = destDir.resolve(fileName);

            Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            selectedThumbnailPath = dest.toAbsolutePath().toString();
            updateThumbnailUI(selectedThumbnailPath);

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Ошибка",
                    "Не удалось сохранить изображение.",
                    "Проверьте права доступа к папке images/thumbnails.");
        }
    }

    private void updateThumbnailUI(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            thumbnailFileLabel.setText("Нет выбранного файла");
            thumbnailPreview.setImage(null);
            return;
        }

        thumbnailFileLabel.setText(shortFileName(storedPath));

        String url;
        if (storedPath.startsWith("http")) {
            url = storedPath;
        } else if (storedPath.startsWith("file:")) {
            url = storedPath;
        } else {
            url = "file:" + storedPath;
        }

        try {
            thumbnailPreview.setImage(new Image(url, true));
        } catch (Exception e) {
            thumbnailPreview.setImage(null);
        }
    }

    private String shortFileName(String path) {
        int idx = path.lastIndexOf(File.separatorChar);
        if (idx >= 0 && idx < path.length() - 1) {
            return path.substring(idx + 1);
        }
        return path;
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText();
        Topic topic = topicCombo.getSelectionModel().getSelectedItem();
        String status = statusCombo.getSelectionModel().getSelectedItem();
        String content = contentArea.getText();

        if (title == null || title.isBlank()
                || topic == null
                || status == null
                || content == null || content.isBlank()) {

            showAlert(Alert.AlertType.WARNING,
                    "Недостаточно данных",
                    null,
                    "Пожалуйста, заполните поля: Заголовок, Тема, Статус и Содержание.");
            return;
        }

        User current = Session.getCurrentUser();
        if (current == null || current.getId() == null) {
            showAlert(Alert.AlertType.ERROR,
                    "Ошибка",
                    null,
                    "Не удалось найти текущего пользователя.");
            return;
        }

        Post p;
        if (editingPost == null) {
            p = new Post();
            p.setCreatedAt(LocalDateTime.now());
            p.setViews(0);
            p.setCommentsCount(0);
            p.setSavedCount(0);
        } else {
            p = editingPost;
            if (p.getCreatedAt() == null) {
                p.setCreatedAt(LocalDateTime.now());
            }
        }

        p.setAuthor(current);
        p.setTitle(title.trim());
        p.setTopic(topic);
        p.setStatus(status); // строка на русском (как в БД)
        p.setContent(content.trim());
        p.setUpdatedAt(LocalDateTime.now());
        p.setThumbnailUrl(selectedThumbnailPath);

        try {
            postService.savePost(p);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Ошибка",
                    "Не удалось сохранить запись.",
                    "Проверьте подключение к базе данных.");
            return;
        }

        if (onSaveCallback != null) {
            try {
                onSaveCallback.run();
            } catch (Exception ignored) {}
        }

        navigateBackToBlogger();
    }

    @FXML
    private void handleCancel() {
        navigateBackToBlogger();
    }

    private void navigateBackToBlogger() {
        if (mainController != null) {
            mainController.openBloggerPage();
        } else {
            closeWindowLegacy();
        }
    }

    private void closeWindowLegacy() {
        if (saveButton != null && saveButton.getScene() != null) {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String safeStr(String s) {
        return s == null ? "" : s;
    }
}
