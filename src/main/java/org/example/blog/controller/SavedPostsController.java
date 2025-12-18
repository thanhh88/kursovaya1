package org.example.blog.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.blog.model.Post;
import org.example.blog.model.User;
import org.example.blog.session.Session;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class SavedPostsController implements MainChildController {

    @FXML private TableView<Post> postTable;

    @FXML private TableColumn<Post, String> colTitle;
    @FXML private TableColumn<Post, String> colAuthor;
    @FXML private TableColumn<Post, String> colTopic;
    @FXML private TableColumn<Post, String> colCreatedAt;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        if (postTable != null) {
            postTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            postTable.setPlaceholder(new Label("Сохранённых записей пока нет."));
        }

        colTitle.setCellValueFactory(cell ->
                new SimpleStringProperty(safe(cell.getValue() != null ? cell.getValue().getTitle() : null, "(без названия)"))
        );

        colAuthor.setCellValueFactory(cell -> {
            Post p = cell.getValue();
            String authorName = (p != null && p.getAuthor() != null)
                    ? safe(p.getAuthor().getFullName(), "(нет автора)")
                    : "(нет автора)";
            return new SimpleStringProperty(authorName);
        });

        colTopic.setCellValueFactory(cell -> {
            Post p = cell.getValue();
            String topicName = (p != null && p.getTopic() != null)
                    ? safe(p.getTopic().getName(), "(без темы)")
                    : "(без темы)";
            return new SimpleStringProperty(topicName);
        });

        colCreatedAt.setCellValueFactory(cell -> {
            Post p = cell.getValue();
            String text = (p != null && p.getCreatedAt() != null)
                    ? p.getCreatedAt().format(dateFormatter)
                    : "";
            return new SimpleStringProperty(text);
        });

        setupRowDoubleClick();
        loadSavedPosts();
    }

    private void loadSavedPosts() {
        User current = Session.getCurrentUser();
        if (current == null || current.getId() == null) {
            postTable.setItems(FXCollections.observableArrayList(Collections.emptyList()));
            return;
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Post> posts = em.createQuery(
                            "SELECT p FROM SavedPost sp " +
                                    "JOIN sp.post p " +              
                                    "JOIN FETCH p.author " +
                                    "JOIN FETCH p.topic " +
                                    "WHERE sp.user.id = :uid " +
                                    "ORDER BY sp.savedAt DESC",
                            Post.class
                    )
                    .setParameter("uid", current.getId())
                    .getResultList();

            postTable.setItems(FXCollections.observableArrayList(posts));
        } catch (Exception e) {
            e.printStackTrace();
            postTable.setItems(FXCollections.observableArrayList(Collections.emptyList()));
        } finally {
            em.close();
        }
    }

    private void setupRowDoubleClick() {
        postTable.setRowFactory(tv -> {
            TableRow<Post> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openPostDetail(row.getItem());
                }
            });
            return row;
        });
    }

    private void openPostDetail(Post post) {
        if (post == null) return;
        if (mainController != null) {
            mainController.openPostDetailPage(post);
        }
    }

    @FXML
    private void handleUnsave() {
        Post selected = postTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) {
            showAlert(Alert.AlertType.INFORMATION,
                    "Информация",
                    null,
                    "Выберите запись в списке, чтобы удалить из сохранённых.");
            return;
        }

        User current = Session.getCurrentUser();
        if (current == null || current.getId() == null) {
            showAlert(Alert.AlertType.WARNING,
                    "Ошибка",
                    null,
                    "Не удалось найти текущего пользователя.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить выбранную запись из сохранённых?");
        var res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            int deleted = em.createQuery(
                            "DELETE FROM SavedPost sp WHERE sp.user.id = :uid AND sp.post.id = :pid"
                    )
                    .setParameter("uid", current.getId())
                    .setParameter("pid", selected.getId())
                    .executeUpdate();
            em.getTransaction().commit();

            if (deleted == 0) {
                showAlert(Alert.AlertType.INFORMATION,
                        "Информация",
                        null,
                        "Запись уже была удалена из сохранённых.");
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Ошибка",
                    null,
                    "Не удалось удалить запись из сохранённых.");
        } finally {
            em.close();
        }

        loadSavedPosts();
    }

    @FXML
    private void handleBackToReader() {
        if (mainController != null) {
            mainController.openReaderPage();
        }
    }

    private String safe(String s, String fallback) {
        if (s == null) return fallback;
        String t = s.trim();
        return t.isEmpty() ? fallback : t;
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
