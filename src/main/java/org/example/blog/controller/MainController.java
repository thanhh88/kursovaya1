package org.example.blog.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.example.blog.dao.UserDaoImpl;
import org.example.blog.model.Post;
import org.example.blog.model.User;
import org.example.blog.service.UserService;
import org.example.blog.service.UserServiceImpl;
import org.example.blog.session.Session;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class MainController {

    @FXML private Label welcomeLabel;
    @FXML private BorderPane rootPane;

    // ⭐ BUTTON CHO MODE TABS
    @FXML private Button readerModeButton;
    @FXML private Button bloggerModeButton;

    private User currentUser;

    private final UserService userService = new UserServiceImpl(new UserDaoImpl());

    // ============================================================
    //  HÀM LOAD FXML CHUNG
    // ============================================================

    private Object setCenterView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof MainChildController) {
                ((MainChildController) controller).setMainController(this);
            }

            rootPane.setCenter(view);
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ============================================================
    //  INIT
    // ============================================================

    public void setCurrentUser(User user) {
        this.currentUser = user;
        Session.setCurrentUser(user);

        if (user != null) {
            welcomeLabel.setText("Здравствуйте, " + user.getFullName());
        } else {
            welcomeLabel.setText("Здравствуйте!");
        }
    }

    @FXML
    private void initialize() {
        currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Здравствуйте, " + currentUser.getFullName());
        }

        // ⭐ Mặc định mở Reader mode
        openReaderPage();
        updateModeTabs("reader");
    }

    // ============================================================
//  MODE READER
// ============================================================

    @FXML
    private void openReader() {
        openReaderPage();
        updateModeTabs("reader");
    }

    public void openReaderPage() {
        Object controller = setCenterView("/org/example/blog/view/reader-view.fxml");
        if (controller instanceof ReaderController) {
            ((ReaderController) controller).reloadPosts();
        }
    }

    // ============================================================
    //  MODE BLOGGER
    // ============================================================

    @FXML
    public void openBlogger() {
        openBloggerPage();
        updateModeTabs("blogger");
    }

    public void openBloggerPage() {
        setCenterView("/org/example/blog/view/blogger-view.fxml");
    }

    // ============================================================
    //  DASHBOARD
    // ============================================================

    @FXML
    private void openDashboard() {
        updateModeTabs(null);
        setCenterView("/org/example/blog/view/dashboard-view.fxml");
    }

    // ============================================================
    //  ACCOUNT
    // ============================================================

    @FXML
    private void openAccount() {
        updateModeTabs(null);
        try {
            User u = Session.getCurrentUser();
            if (u != null) {
                User fresh = userService.findById(u.getId());
                Session.setCurrentUser(fresh);
            }
            setCenterView("/org/example/blog/view/account-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    //  SAVED POSTS
    // ============================================================

    @FXML
    private void openSavedPosts() {
        updateModeTabs(null);
        setCenterView("/org/example/blog/view/saved-posts-view.fxml");
    }

    // ============================================================
    //  POST DETAIL
    // ============================================================

    public void openPostDetailPage(Post post) {
        if (post == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/blog/view/post-detail-view.fxml")
            );
            Node content = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof MainChildController mc) mc.setMainController(this);
            if (ctrl instanceof PostDetailController pc) pc.setPost(post);

            rootPane.setCenter(content);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    //  POST FORM (TẠO / SỬA)
    // ============================================================

    public void openPostFormPage(Post editingPost) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/blog/view/post-form-view.fxml")
            );
            Node content = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof MainChildController mc) mc.setMainController(this);
            if (ctrl instanceof PostFormController form) form.setEditingPost(editingPost);

            rootPane.setCenter(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // открыть страницу "Автор" (вешается на кнопку в левом меню)
    @FXML
    private void openAuthor() {
        openAuthorPage();
    }

    public void openAuthorPage() {
        setCenterView("/org/example/blog/view/author-view.fxml");
    }

    // ============================================================
    //  ⭐ UPDATE MODE TABS UI
    // ============================================================

    public void updateModeTabs(String mode) {

        if (readerModeButton == null || bloggerModeButton == null) return;

        // Reset hết
        readerModeButton.getStyleClass().removeAll("mode-tab-active");
        bloggerModeButton.getStyleClass().removeAll("mode-tab-active");

        readerModeButton.getStyleClass().add("mode-tab");
        bloggerModeButton.getStyleClass().add("mode-tab");

        // Active tab đang chọn
        if ("reader".equals(mode)) {
            readerModeButton.getStyleClass().add("mode-tab-active");
        } else if ("blogger".equals(mode)) {
            bloggerModeButton.getStyleClass().add("mode-tab-active");
        }
    }

    // ============================================================
    //  LOGOUT → Quay lại Landing page
    // ============================================================

    @FXML
    private void handleLogout() {
        Session.setCurrentUser(null);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/blog/view/landing-view.fxml")
            );
            Parent landingRoot = loader.load();

            Scene scene = rootPane.getScene();
            scene.setRoot(landingRoot);

            Stage stage = (Stage) scene.getWindow();
            stage.setTitle("Blog System - Landing");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
