package org.example.blog.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.blog.dao.UserDaoImpl;
import org.example.blog.model.User;
import org.example.blog.service.UserService;
import org.example.blog.service.UserServiceImpl;
import org.example.blog.session.Session;

import java.io.IOException;
import java.util.Locale;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final UserService userService = new UserServiceImpl(new UserDaoImpl());

    @FXML
    private void initialize() {
        // nothing special yet
    }

    // ---------- ВХОД (LOGIN) ----------
    @FXML
    private void handleLogin(ActionEvent event) {
        messageLabel.setText("");

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username != null) {
            username = username.trim().toLowerCase(Locale.ROOT);
        }
        if (password == null) password = "";

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Пожалуйста, введите имя пользователя и пароль.");
            return;
        }

        User logged = userService.login(username, password);

        if (logged == null) {
            messageLabel.setText("Неверное имя пользователя или пароль.");
            return;
        }

        Session.setCurrentUser(logged);
        openMainView(logged);
    }

    private void openMainView(User loggedUser) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/blog/view/main-view.fxml")
            );
            Parent newRoot = loader.load();

            MainController mainController = loader.getController();
            mainController.setCurrentUser(loggedUser);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = stage.getScene();

            scene.setRoot(newRoot);

            stage.setTitle("Блог-система - Главная");
            stage.setMinWidth(900);
            stage.setMinHeight(650);

        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Ошибка при открытии главного окна.");
        }
    }

    // ---------- ПЕРЕЙТИ НА РЕГИСТРАЦИЮ ----------
    @FXML
    private void handleShowRegister(ActionEvent event) {
        switchScene("/org/example/blog/view/register-view.fxml",
                "Блог-система - Регистрация");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newRoot = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = stage.getScene();

            scene.setRoot(newRoot);
            stage.setTitle(title);

        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Не удалось открыть окно: " + title);
        }
    }
}
