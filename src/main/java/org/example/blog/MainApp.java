package org.example.blog;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/blog/view/landing-view.fxml")
        );

        Parent root = loader.load();

        Scene scene = new Scene(root);

        primaryStage.setTitle("Blog System");
        primaryStage.setScene(scene);

        // Cho phép resize + kích thước tối thiểu
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        // ⭐⭐ MỞ FULL MÀN HÌNH KHI CHẠY ⭐⭐
        primaryStage.setMaximized(true);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
