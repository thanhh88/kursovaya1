package org.example.blog.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.example.blog.model.Post;
import org.example.blog.model.User;
import org.example.blog.session.Session;
import org.example.blog.util.JpaUtil;

import javax.persistence.EntityManager;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SavedPostsController implements MainChildController {

    @FXML
    private TableView<Post> postTable;

    @FXML
    private TableColumn<Post, String> colTitle;

    @FXML
    private TableColumn<Post, String> colAuthor;

    @FXML
    private TableColumn<Post, String> colTopic;

    @FXML
    private TableColumn<Post, String> colCreatedAt;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        colTitle.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getTitle())
        );

        colAuthor.setCellValueFactory(cell -> {
            Post p = cell.getValue();
            String authorName = (p.getAuthor() != null && p.getAuthor().getFullName() != null)
                    ? p.getAuthor().getFullName()
                    : "(нет автора)";
            return new SimpleStringProperty(authorName);
        });

        colTopic.setCellValueFactory(cell -> {
            Post p = cell.getValue();
            String topicName = (p.getTopic() != null && p.getTopic().getName() != null)
                    ? p.getTopic().getName()
                    : "(без темы)";
            return new SimpleStringProperty(topicName);
        });

        colCreatedAt.setCellValueFactory(cell -> {
            Post p = cell.getValue();
            String text = (p.getCreatedAt() != null)
                    ? p.getCreatedAt().format(dateFormatter)
                    : "";
            return new SimpleStringProperty(text);
        });

        setupRowDoubleClick();
        loadSavedPosts();
    }

    private void loadSavedPosts() {
        User current = Session.getCurrentUser();
        if (current == null) {
            System.out.println("SavedPostsController: нет текущего пользователя, таблица пустая.");
            postTable.setItems(FXCollections.observableArrayList());
            return;
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Post> posts = em.createQuery(
                            "SELECT p FROM SavedPost sp " +
                                    "JOIN sp.post p " +
                                    "JOIN FETCH p.author " +
                                    "JOIN FETCH p.topic " +
                                    "WHERE sp.user = :user",
                            Post.class
                    )
                    .setParameter("user", current)
                    .getResultList();

            System.out.println("SavedPostsController.loadSavedPosts() -> size = " + posts.size());
            postTable.setItems(FXCollections.observableArrayList(posts));
        } finally {
            em.close();
        }
    }

    private void setupRowDoubleClick() {
        postTable.setRowFactory(tv -> {
            TableRow<Post> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Post selected = row.getItem();
                    openPostDetail(selected);
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
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информация");
            alert.setHeaderText(null);
            alert.setContentText("Выберите запись в списке, чтобы удалить из сохранённых.");
            alert.showAndWait();
            return;
        }

        User current = Session.getCurrentUser();
        if (current == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Не удалось найти текущего пользователя.");
            alert.showAndWait();
            return;
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            int deleted = em.createQuery(
                            "DELETE FROM SavedPost sp WHERE sp.user = :user AND sp.post = :post")
                    .setParameter("user", current)
                    .setParameter("post", selected)
                    .executeUpdate();
            em.getTransaction().commit();

            System.out.println("Unsave result, rows deleted = " + deleted);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
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
}
