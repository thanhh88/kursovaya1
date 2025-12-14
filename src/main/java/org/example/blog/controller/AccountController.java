package org.example.blog.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.example.blog.dao.TopicDAO;
import org.example.blog.dao.UserDaoImpl;
import org.example.blog.model.Topic;
import org.example.blog.model.User;
import org.example.blog.service.UserService;
import org.example.blog.service.UserServiceImpl;
import org.example.blog.session.Session;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountController implements MainChildController {

    @FXML private Label usernameLabel;
    @FXML private TextField fullNameField;
    @FXML private Spinner<Integer> ageSpinner;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> countryCombo;
    @FXML private TextArea bioArea;
    @FXML private ListView<Topic> favoriteTopicsListView;
    @FXML private Label messageLabel;

    @FXML private ImageView avatarImageView;
    @FXML private Label avatarFileLabel;

    private final TopicDAO topicDAO = new TopicDAO();
    private final UserService userService = new UserServiceImpl(new UserDaoImpl());

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        User user = Session.getCurrentUser();
        if (user == null) {
            messageLabel.setText("Пользователь в текущей сессии не найден!");
            disableForm();
            return;
        }

        initControls();

        List<Topic> allTopics = topicDAO.findAll();
        favoriteTopicsListView.getItems().setAll(allTopics);

        fillFormFromUser(user, allTopics);
    }

    private void initControls() {
        ageSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 18)
        );

        // Пол
        genderCombo.getItems().setAll("Мужской", "Женский", "Другое");

        // Статус
        statusCombo.getItems().setAll(
                "Школьник / Студент",
                "Работающий",
                "Другое"
        );

        // Страны
        countryCombo.getItems().setAll(
                "Россия", "Вьетнам", "Япония", "Китай", "США",
                "Таиланд", "Корея", "Великобритания", "Франция", "Германия", "Другая"
        );

        favoriteTopicsListView.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void disableForm() {
        fullNameField.setDisable(true);
        ageSpinner.setDisable(true);
        genderCombo.setDisable(true);
        statusCombo.setDisable(true);
        countryCombo.setDisable(true);
        bioArea.setDisable(true);
        favoriteTopicsListView.setDisable(true);
    }

    private void fillFormFromUser(User user, List<Topic> allTopics) {
        usernameLabel.setText(user.getUsername());

        if (user.getFullName() != null) {
            fullNameField.setText(user.getFullName());
        }
        if (user.getAge() != null) {
            ageSpinner.getValueFactory().setValue(user.getAge());
        }
        if (user.getGender() != null) {
            genderCombo.setValue(user.getGender());
        }
        if (user.getStatus() != null) {
            statusCombo.setValue(user.getStatus());
        }
        if (user.getCountry() != null) {
            countryCombo.setValue(user.getCountry());
        }
        if (user.getBio() != null) {
            bioArea.setText(user.getBio());
        }

        // avatar
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            avatarFileLabel.setText(shortFileName(user.getAvatarUrl()));
            updateAvatarImage(user.getAvatarUrl());
        } else {
            avatarFileLabel.setText("Нет выбранного файла");
            avatarImageView.setImage(null);
        }

        if (user.getFavoriteTopics() != null) {
            for (Topic fav : user.getFavoriteTopics()) {
                for (Topic item : allTopics) {
                    if (fav.getId() != null && fav.getId().equals(item.getId())) {
                        favoriteTopicsListView.getSelectionModel().select(item);
                    }
                }
            }
        }
    }

    private String shortFileName(String path) {
        int idx = path.lastIndexOf(File.separatorChar);
        if (idx >= 0 && idx < path.length() - 1) {
            return path.substring(idx + 1);
        }
        return path;
    }

    private void updateAvatarImage(String storedPath) {
        try {
            if (storedPath == null || storedPath.isBlank()) {
                avatarImageView.setImage(null);
                return;
            }

            String url;
            if (storedPath.startsWith("http")) {
                url = storedPath;
            } else if (storedPath.startsWith("file:")) {
                url = storedPath;
            } else {
                url = "file:" + storedPath;
            }

            avatarImageView.setImage(new Image(url, true));
        } catch (Exception e) {
            avatarImageView.setImage(null);
        }
    }

    @FXML
    private void handleChooseAvatar() {
        User user = Session.getCurrentUser();
        if (user == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите фото для аватара");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        java.io.File selected = chooser.showOpenDialog(
                avatarImageView.getScene().getWindow()
        );
        if (selected == null) return;

        try {
            Path destDir = Paths.get("images", "avatars");
            Files.createDirectories(destDir);

            String fileName = System.currentTimeMillis() + "_" + selected.getName();
            Path dest = destDir.resolve(fileName);

            Files.copy(selected.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            String storedPath = dest.toAbsolutePath().toString();
            user.setAvatarUrl(storedPath);
            Session.setCurrentUser(user);

            avatarFileLabel.setText(selected.getName());
            updateAvatarImage(storedPath);

        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setText("Не удалось сохранить аватар.");
        }
    }

    @FXML
    private void handleSaveProfile() {
        User user = Session.getCurrentUser();
        if (user == null) {
            messageLabel.setText("Пользователь не существует в сессии.");
            return;
        }

        user.setFullName(fullNameField.getText());
        user.setAge(ageSpinner.getValue());
        user.setGender(genderCombo.getValue());
        user.setStatus(statusCombo.getValue());
        user.setCountry(countryCombo.getValue());
        user.setBio(bioArea.getText());

        Set<Topic> selectedTopics =
                new HashSet<>(favoriteTopicsListView.getSelectionModel().getSelectedItems());
        user.setFavoriteTopics(selectedTopics);

        if (!validateProfile(user)) {
            return;
        }

        boolean ok = userService.updateProfile(user);

        if (ok) {
            messageLabel.setText("Изменения сохранены ✔");
            Session.setCurrentUser(user);
        } else {
            messageLabel.setText("Не удалось сохранить изменения. Попробуйте ещё раз.");
        }
    }

    private boolean validateProfile(User user) {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            messageLabel.setText("ФИО не может быть пустым.");
            return false;
        }
        if (user.getAge() != null && user.getAge() < 0) {
            messageLabel.setText("Возраст указан некорректно.");
            return false;
        }
        return true;
    }

    @FXML
    private void handleBackToMain() {
        if (mainController != null) {
            mainController.openReaderPage();
        }
    }
}
