package org.example.blog.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LandingController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private void handleGoToLogin() {
        switchScene("/org/example/blog/view/login-view.fxml",
                "Блог-система - Вход");
    }

    @FXML
    private void handleGoToRegister() {
        switchScene("/org/example/blog/view/register-view.fxml",
                "Блог-система - Регистрация");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath)
            );
            Parent newRoot = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = stage.getScene();

            // 🔥 Không tạo Scene mới, chỉ đổi root
            scene.setRoot(newRoot);
            stage.setTitle(title);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
