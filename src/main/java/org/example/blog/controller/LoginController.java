package org.example.blog.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserServiceImpl(new UserDaoImpl());

    @FXML
    private void initialize() {
        messageLabel.setText("");
    }

    // ВХОД (LOGIN)
    @FXML
    private void handleLogin(ActionEvent event) {
        messageLabel.setText("");

        String username = usernameField.getText();
        String password = passwordField.getText();

        // giữ logic của bạn: trim + toLowerCase để thống nhất với Register
        if (username != null) {
            username = username.trim().toLowerCase(Locale.ROOT);
        } else {
            username = "";
        }

        if (password == null) password = "";

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Пожалуйста, введите имя пользователя и пароль.");
            return;
        }

        try {
            User logged = userService.login(username, password);

            if (logged == null) {
                messageLabel.setText("Неверное имя пользователя или пароль.");
                return;
            }

            Session.setCurrentUser(logged);
            openMainView(logged);

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Ошибка", "Не удалось выполнить вход.",
                    "Проверьте подключение к базе данных и попробуйте снова.");
        }
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
            showAlert("Ошибка", "Не удалось открыть главное окно.",
                    "Попробуйте перезапустить приложение.");
        }
    }

    // ПЕРЕЙТИ НА РЕГИСТРАЦИЮ
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
            showAlert("Ошибка", "Не удалось открыть окно.", title);
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
