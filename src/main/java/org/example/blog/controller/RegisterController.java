package org.example.blog.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.blog.dao.TopicDAO;
import org.example.blog.dao.UserDaoImpl;
import org.example.blog.model.Topic;
import org.example.blog.model.User;
import org.example.blog.service.UserService;
import org.example.blog.service.UserServiceImpl;

import java.text.Collator;
import java.util.*;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private Spinner<Integer> ageSpinner;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> countryCombo;
    @FXML private TextArea bioArea;
    @FXML private ListView<Topic> topicListView;
    @FXML private Label messageLabel;

    private final UserService userService = new UserServiceImpl(new UserDaoImpl());
    private final TopicDAO topicDAO = new TopicDAO();

    // username: только [a-z0-9_.-], длина 4–20
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-z0-9_.-]{4,20}$");

    // ФИО: буквы + пробел, длина 3–60
    private static final Pattern FULLNAME_PATTERN =
            Pattern.compile("^[\\p{L}\\s-]{3,60}$");

    @FXML
    private void initialize() {
        messageLabel.setText("");

        // Возраст
        ageSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 18)
        );

        // Пол
        genderCombo.setItems(FXCollections.observableArrayList(
                "Мужской", "Женский", "Другое"
        ));
        genderCombo.getSelectionModel().clearSelection();

        // Статус
        statusCombo.setItems(FXCollections.observableArrayList(
                "Школьник / Студент",
                "Работающий",
                "Другое"
        ));
        statusCombo.getSelectionModel().clearSelection();

        // Страна
        loadCountries();

        // Темы из БД
        loadTopicsForListView();
    }

    /** Загружает все страны мира и заполняет ComboBox подписями на русском. */
    private void loadCountries() {
        String[] countryCodes = Locale.getISOCountries();
        List<String> countries = new ArrayList<>();
        Locale ru = new Locale("ru");

        for (String code : countryCodes) {
            Locale locale = new Locale("", code);
            String name = locale.getDisplayCountry(ru);
            if (name != null && !name.isBlank()) {
                countries.add(name);
            }
        }

        countries.sort(Collator.getInstance(ru));
        countryCombo.setItems(FXCollections.observableArrayList(countries));
        countryCombo.getSelectionModel().clearSelection();
    }

    /** Загружает темы в ListView и настраивает отображение. */
    private void loadTopicsForListView() {
        List<Topic> topics;
        try {
            topics = topicDAO.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            topics = Collections.emptyList();
            messageLabel.setText("Не удалось загрузить темы из базы данных.");
        }

        Locale ru = new Locale("ru");
        Collator collator = Collator.getInstance(ru);
        topics.sort((t1, t2) -> {
            String n1 = t1.getName() != null ? t1.getName() : "";
            String n2 = t2.getName() != null ? t2.getName() : "";
            return collator.compare(n1, n2);
        });

        ObservableList<Topic> observableTopics = FXCollections.observableArrayList(topics);
        topicListView.setItems(observableTopics);
        topicListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        topicListView.setCellFactory(lv -> {
            ListCell<Topic> cell = new ListCell<>() {
                @Override
                protected void updateItem(Topic item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String name = item.getName() != null ? item.getName() : "(без названия)";
                        setText(name);
                    }
                }
            };
            cell.setWrapText(true);
            cell.setPrefWidth(0);
            return cell;
        });
    }

    //  КНОПКА "Зарегистрироваться"
    @FXML
    private void handleRegister() {
        messageLabel.setText("");

        String username = safeTrim(usernameField.getText()).toLowerCase(Locale.ROOT);
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        String confirmPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();
        String fullName = safeTrim(fullNameField.getText());
        Integer age = ageSpinner.getValue();
        String gender = genderCombo.getValue();
        String status = statusCombo.getValue();
        String country = countryCombo.getValue();
        String bio = bioArea.getText() == null ? "" : bioArea.getText().trim();
        ObservableList<Topic> selectedTopics = topicListView.getSelectionModel().getSelectedItems();

        if (!validateInputs(username, password, confirmPassword, fullName, age, gender, status, country, selectedTopics)) {
            return;
        }

        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(password);
        u.setFullName(fullName);
        u.setAge(age);
        u.setGender(gender);
        u.setStatus(status);
        u.setCountry(country);
        u.setBio(bio);

        Set<Topic> fav = new HashSet<>(selectedTopics);
        u.setFavoriteTopics(fav);

        boolean ok;
        try {
            ok = userService.register(u);
        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setText("Ошибка регистрации. Проверьте подключение к БД.");
            return;
        }

        if (!ok) {
            messageLabel.setText("Имя пользователя уже существует.");
            return;
        }

        messageLabel.setText("Регистрация прошла успешно! Сейчас вы будете перенаправлены на страницу входа...");
        goBackToLogin();
    }

    // КНОПКА "Уже есть аккаунт? Войти"
    @FXML
    private void handleBackToLogin() {
        goBackToLogin();
    }

    private void goBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/blog/view/login-view.fxml")
            );
            Parent newRoot = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = stage.getScene();

            scene.setRoot(newRoot);
            stage.setTitle("Блог-система - Вход");

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Не удалось открыть окно входа.");
        }
    }

    // ВАЛИДАЦИЯ

    private boolean validateInputs(String username,
                                   String password,
                                   String confirmPassword,
                                   String fullName,
                                   Integer age,
                                   String gender,
                                   String status,
                                   String country,
                                   List<Topic> selectedTopics) {

        if (username.isEmpty()) {
            messageLabel.setText("Имя пользователя не может быть пустым.");
            return false;
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            messageLabel.setText(
                    "Имя пользователя должно содержать от 4 до 20 символов: " +
                            "латинские буквы в нижнем регистре, цифры, а также '.', '_' или '-'.");
            return false;
        }

        if (password.length() < 6) {
            messageLabel.setText("Пароль слишком короткий (минимум 6 символов).");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Пароль и подтверждение пароля не совпадают.");
            return false;
        }

        if (fullName.isEmpty()) {
            messageLabel.setText("ФИО не может быть пустым.");
            return false;
        }
        if (!FULLNAME_PATTERN.matcher(fullName).matches()) {
            messageLabel.setText(
                    "ФИО должно содержать только буквы, пробелы и дефисы (от 3 до 60 символов).");
            return false;
        }

        if (age == null || age < 14) {
            messageLabel.setText("Возраст должен быть не меньше 14 лет.");
            return false;
        }

        if (gender == null || gender.isBlank()) {
            messageLabel.setText("Пожалуйста, выберите пол.");
            return false;
        }
        if (status == null || status.isBlank()) {
            messageLabel.setText("Пожалуйста, выберите статус.");
            return false;
        }
        if (country == null || country.isBlank()) {
            messageLabel.setText("Пожалуйста, выберите страну.");
            return false;
        }

        if (selectedTopics == null || selectedTopics.isEmpty()) {
            messageLabel.setText("Выберите хотя бы одну любимую тему.");
            return false;
        }

        if (bioArea.getText() != null && bioArea.getText().length() > 1000) {
            messageLabel.setText("Биография слишком длинная (максимум 1000 символов).");
            return false;
        }

        return true;
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
