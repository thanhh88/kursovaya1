package org.example.blog.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.example.blog.model.Post;
import org.example.blog.model.PostStatus;
import org.example.blog.model.User;
import org.example.blog.service.PostService;
import org.example.blog.session.Session;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class BloggerController implements MainChildController {

    @FXML
    private TableView<Post> postTable;

    @FXML
    private TableColumn<Post, String> colTitle;

    @FXML
    private TableColumn<Post, String> colTopic;

    @FXML
    private TableColumn<Post, String> colStatus;

    @FXML
    private TableColumn<Post, String> colCreatedAt;

    @FXML
    private TableColumn<Post, Number> colViews;

    @FXML
    private TableColumn<Post, Number> colComments;

    @FXML
    private TableColumn<Post, Number> colSaved;

    private final PostService postService = new PostService();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        postTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        postTable.setSortPolicy(tv -> null);
        postTable.setPlaceholder(new Label("У вас ещё нет записей."));

        colTitle.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTitle()));

        colTopic.setCellValueFactory(c -> {
            Post p = c.getValue();
            String topicName = (p.getTopic() != null && p.getTopic().getName() != null)
                    ? p.getTopic().getName()
                    : "(без темы)";
            return new SimpleStringProperty(topicName);
        });

        // 🔹 Dùng PostStatus (строки на русском) вместо "PUBLISHED"/"DRAFT"
        colStatus.setCellValueFactory(c -> {
            String status = c.getValue().getStatus();
            String display;
            if (PostStatus.PUBLISHED.equals(status)) {
                display = "Опубликовано";
            } else if (PostStatus.DRAFT.equals(status)) {
                display = "Черновик";
            } else {
                display = status != null ? status : "";
            }
            return new SimpleStringProperty(display);
        });

        TableColumn<?, ?>[] cols = {
                colTitle, colTopic, colStatus,
                colCreatedAt, colViews, colComments, colSaved
        };

        for (TableColumn<?, ?> c : cols) {
            if (c != null) {
                c.setSortable(false);
                c.setReorderable(false);
            }
        }

        colCreatedAt.setCellValueFactory(c -> {
            Post p = c.getValue();
            String text = (p.getCreatedAt() != null)
                    ? p.getCreatedAt().format(dateFormatter)
                    : "";
            return new SimpleStringProperty(text);
        });

        colViews.setCellValueFactory(c ->
                new SimpleIntegerProperty(safeInt(c.getValue().getViews())));
        colComments.setCellValueFactory(c ->
                new SimpleIntegerProperty(safeInt(c.getValue().getCommentsCount())));
        colSaved.setCellValueFactory(c ->
                new SimpleIntegerProperty(safeInt(c.getValue().getSavedCount())));

        setupRowDoubleClick();
        loadPostsOfCurrentUser();
    }

    private int safeInt(Integer v) {
        return v != null ? v : 0;
    }

    private void loadPostsOfCurrentUser() {
        User current = Session.getCurrentUser();
        if (current == null) {
            System.out.println("BloggerController: нет текущего пользователя, таблица пустая.");
            postTable.setItems(FXCollections.observableArrayList());
            return;
        }

        List<Post> posts = postService.getPostsByAuthor(current);
        System.out.println("BloggerController.loadPostsOfCurrentUser() -> size = " + posts.size());
        postTable.setItems(FXCollections.observableArrayList(posts));
    }

    private void setupRowDoubleClick() {
        postTable.setRowFactory(tv -> {
            TableRow<Post> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Post selected = row.getItem();
                    openPostForm(selected);
                }
            });
            return row;
        });
    }

    @FXML
    private void handleRefresh() {
        loadPostsOfCurrentUser();
    }

    @FXML
    private void handleNewPost() {
        openPostForm(null);
    }

    @FXML
    private void handleEditPost() {
        Post selected = postTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информация");
            alert.setHeaderText(null);
            alert.setContentText("Выберите запись для редактирования.");
            alert.showAndWait();
            return;
        }
        openPostForm(selected);
    }

    @FXML
    private void handleDeletePost() {
        Post selected = postTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информация");
            alert.setHeaderText(null);
            alert.setContentText("Выберите запись для удаления.");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите удалить запись:\n\"" + selected.getTitle() + "\" ?");
        var result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            postService.deletePost(selected);
            loadPostsOfCurrentUser();
        }
    }

    private void openPostForm(Post post) {
        if (mainController != null) {
            mainController.openPostFormPage(post);
        } else {
            System.out.println("BloggerController: mainController is null, cannot open post form.");
        }
    }
}
