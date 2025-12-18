package org.example.blog.controller;

import javafx.collections.FXCollections;
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
import java.text.Collator;
import java.nio.file.*;
import java.util.*;

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
        if (user == null || user.getId() == null) {
            messageLabel.setText("Пользователь в текущей сессии не найден!");
            disableForm();
            return;
        }

        initControls();

        List<Topic> allTopics = topicDAO.findAll();
        // sort topics ru
        Locale ru = new Locale("ru");
        Collator coll = Collator.getInstance(ru);
        allTopics.sort(Comparator.comparing(t -> t.getName() != null ? t.getName() : "", coll));

        favoriteTopicsListView.getItems().setAll(allTopics);

        fillFormFromUser(user, allTopics);
    }

    private void initControls() {
        ageSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 18)
        );

        genderCombo.getItems().setAll("Мужской", "Женский", "Другое");
        statusCombo.getItems().setAll("Школьник / Студент", "Работающий", "Другое");

        loadCountriesRu();

        favoriteTopicsListView.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);

        if (avatarFileLabel != null) avatarFileLabel.setText("Нет выбранного файла");
        if (messageLabel != null) messageLabel.setText("");
    }

    private void loadCountriesRu() {
        String[] codes = Locale.getISOCountries();
        List<String> countries = new ArrayList<>();
        Locale ru = new Locale("ru");

        for (String code : codes) {
            Locale loc = new Locale("", code);
            String name = loc.getDisplayCountry(ru);
            if (name != null && !name.isBlank()) {
                countries.add(name);
            }
        }
        countries.sort(Collator.getInstance(ru));
        countryCombo.setItems(FXCollections.observableArrayList(countries));
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
        usernameLabel.setText(safe(user.getUsername(), ""));

        fullNameField.setText(safe(user.getFullName(), ""));
        if (user.getAge() != null) ageSpinner.getValueFactory().setValue(user.getAge());

        if (user.getGender() != null) genderCombo.setValue(user.getGender());
        if (user.getStatus() != null) statusCombo.setValue(user.getStatus());
        if (user.getCountry() != null) countryCombo.setValue(user.getCountry());
        bioArea.setText(safe(user.getBio(), ""));

        // avatar
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            avatarFileLabel.setText(shortFileName(user.getAvatarUrl()));
            updateAvatarImage(user.getAvatarUrl());
        } else {
            avatarFileLabel.setText("Нет выбранного файла");
            avatarImageView.setImage(null);
        }

        // favorite topics
        favoriteTopicsListView.getSelectionModel().clearSelection();
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
        if (idx >= 0 && idx < path.length() - 1) return path.substring(idx + 1);
        return path;
    }

    private void updateAvatarImage(String storedPath) {
        try {
            if (storedPath == null || storedPath.isBlank()) {
                avatarImageView.setImage(null);
                return;
            }

            String url;
            if (storedPath.startsWith("http")) url = storedPath;
            else if (storedPath.startsWith("file:")) url = storedPath;
            else url = "file:" + storedPath;

            avatarImageView.setImage(new Image(url, true));
        } catch (Exception e) {
            avatarImageView.setImage(null);
        }
    }

    @FXML
    private void handleChooseAvatar() {
        User user = Session.getCurrentUser();
        if (user == null || user.getId() == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите фото для аватара");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selected = chooser.showOpenDialog(avatarImageView.getScene().getWindow());
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

            messageLabel.setText("Аватар обновлён ✔");
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", null, "Не удалось сохранить аватар.");
        }
    }

    @FXML
    private void handleSaveProfile() {
        User user = Session.getCurrentUser();
        if (user == null || user.getId() == null) {
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

        if (!validateProfile(user)) return;

        try {
            boolean ok = userService.updateProfile(user);
            if (ok) {
                messageLabel.setText("Изменения сохранены ✔");
                // обновим сессию (не меняет логику, просто делает данные “fresh”)
                User fresh = userService.findById(user.getId());
                if (fresh != null) Session.setCurrentUser(fresh);
            } else {
                messageLabel.setText("Не удалось сохранить изменения. Попробуйте ещё раз.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Ошибка при сохранении профиля.");
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
        if (user.getCountry() == null || user.getCountry().isBlank()) {
            messageLabel.setText("Пожалуйста, выберите страну.");
            return false;
        }
        if (user.getFavoriteTopics() == null || user.getFavoriteTopics().isEmpty()) {
            messageLabel.setText("Выберите хотя бы одну любимую тему.");
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
