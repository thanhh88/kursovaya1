package org.example.blog.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.example.blog.model.PostStatus;
import org.example.blog.model.User;
import org.example.blog.service.DashboardService;
import org.example.blog.service.DashboardService.ReaderTopicStat;
import org.example.blog.service.DashboardService.TopicPostCount;
import org.example.blog.service.DashboardService.TopPostStat;
import org.example.blog.session.Session;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController implements MainChildController {

    @FXML private Label lblTotalPosts;
    @FXML private Label lblPublished;
    @FXML private Label lblDraft;
    @FXML private Label lblComments;
    @FXML private Label lblSaved;
    @FXML private Label lblTotalViews;

    @FXML private TableView<TopicPostCount> topicTable;
    @FXML private TableColumn<TopicPostCount, String> colTopicName;
    @FXML private TableColumn<TopicPostCount, Number> colTopicCount;
    @FXML private PieChart bloggerTopicsPieChart;

    @FXML private TableView<TopPostStat> topPostsTable;
    @FXML private TableColumn<TopPostStat, String> colTopTitle;
    @FXML private TableColumn<TopPostStat, Number> colTopViews;
    @FXML private TableColumn<TopPostStat, Number> colTopComments;
    @FXML private TableColumn<TopPostStat, Number> colTopSaved;
    @FXML private TableColumn<TopPostStat, Number> colTopEngagement;

    @FXML private Label lblReaderSaved;
    @FXML private Label lblReaderCommentedPosts;
    @FXML private Label lblReaderViewed;
    @FXML private Label lblReaderStreak;

    @FXML private LineChart<String, Number> readerViewsChart;
    @FXML private PieChart readerTopicsPieChart;

    @FXML private Label messageLabel;

    private final DashboardService dashboardService = new DashboardService();
    private User currentUser;

    private MainController mainController;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd.MM");

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        currentUser = Session.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Нет активного пользователя (пользователь не авторизован).");
            return;
        }

        if (topicTable != null) {
            topicTable.setPlaceholder(new Label("Нет данных по темам."));
        }
        if (topPostsTable != null) {
            topPostsTable.setPlaceholder(new Label("Нет данных для Top Posts."));
        }

        if (colTopicName != null) {
            colTopicName.setCellValueFactory(cell ->
                    new SimpleStringProperty(cell.getValue().getTopicName()));
        }
        if (colTopicCount != null) {
            colTopicCount.setCellValueFactory(cell ->
                    new SimpleLongProperty(cell.getValue().getCount()));
        }

        if (colTopTitle != null) {
            colTopTitle.setCellValueFactory(cell ->
                    new SimpleStringProperty(cell.getValue().getTitle()));
        }
        if (colTopViews != null) {
            colTopViews.setCellValueFactory(cell ->
                    new SimpleLongProperty(cell.getValue().getViews()));
        }
        if (colTopComments != null) {
            colTopComments.setCellValueFactory(cell ->
                    new SimpleLongProperty(cell.getValue().getComments()));
        }
        if (colTopSaved != null) {
            colTopSaved.setCellValueFactory(cell ->
                    new SimpleLongProperty(cell.getValue().getSaved()));
        }
        if (colTopEngagement != null) {
            colTopEngagement.setCellValueFactory(cell ->
                    new SimpleDoubleProperty(cell.getValue().getEngagementScore()));
        }

        loadStats();
    }

    private void loadStats() {
        try {
            long total = dashboardService.countPosts(currentUser);

            //ВАЖНО: статусы хранятся в БД на русском (PostStatus)
            long published = dashboardService.countPostsByStatus(currentUser, PostStatus.PUBLISHED);
            long draft = dashboardService.countPostsByStatus(currentUser, PostStatus.DRAFT);

            long comments = dashboardService.countComments(currentUser);
            long saved = dashboardService.countSavedPosts(currentUser);
            long totalViews = dashboardService.countTotalViews(currentUser);

            List<TopicPostCount> topicCounts =
                    dashboardService.countPostsByTopic(currentUser);

            lblTotalPosts.setText(String.valueOf(total));
            lblPublished.setText(String.valueOf(published));
            lblDraft.setText(String.valueOf(draft));
            lblComments.setText(String.valueOf(comments));
            lblSaved.setText(String.valueOf(saved));
            lblTotalViews.setText(String.valueOf(totalViews));

            if (topicTable != null) {
                topicTable.setItems(FXCollections.observableArrayList(topicCounts));
            }

            if (bloggerTopicsPieChart != null) {
                bloggerTopicsPieChart.getData().clear();
                for (TopicPostCount tc : topicCounts) {
                    bloggerTopicsPieChart.getData().add(
                            new PieChart.Data(tc.getTopicName(), tc.getCount())
                    );
                }
            }

            List<TopPostStat> topPosts =
                    dashboardService.getTopPostsByEngagement(currentUser, 5);
            if (topPostsTable != null) {
                ObservableList<TopPostStat> data =
                        FXCollections.observableArrayList(topPosts);
                topPostsTable.setItems(data);
            }

            // Reader stats
            long readerSaved = dashboardService.countSavedPosts(currentUser);
            long readerCommentedPosts = dashboardService.countCommentedPosts(currentUser);
            long readerViewed = dashboardService.countViewedPostsByUser(currentUser);
            int streak = dashboardService.getReadingStreak(currentUser);

            if (lblReaderSaved != null) lblReaderSaved.setText(String.valueOf(readerSaved));
            if (lblReaderCommentedPosts != null) lblReaderCommentedPosts.setText(String.valueOf(readerCommentedPosts));
            if (lblReaderViewed != null) lblReaderViewed.setText(String.valueOf(readerViewed));
            if (lblReaderStreak != null) lblReaderStreak.setText(String.valueOf(streak));

            // Views chart (30 days)
            if (readerViewsChart != null) {
                readerViewsChart.getData().clear();

                LocalDate to = LocalDate.now();
                LocalDate from = to.minusDays(29);

                Map<LocalDate, Long> dailyViews =
                        dashboardService.getDailyViewsForUser(currentUser, from, to);

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Просмотры");

                List<LocalDate> days = dailyViews.keySet().stream()
                        .sorted()
                        .collect(Collectors.toList());

                for (LocalDate day : days) {
                    Long count = dailyViews.get(day);
                    series.getData().add(
                            new XYChart.Data<>(day.format(dateFormatter), count != null ? count : 0L)
                    );
                }

                readerViewsChart.getData().add(series);
            }

            // Reader topics pie
            if (readerTopicsPieChart != null) {
                readerTopicsPieChart.getData().clear();

                List<ReaderTopicStat> readerTopicStats =
                        dashboardService.getReaderTopicStats(currentUser);

                int MAX_SLICE = 6;
                List<ReaderTopicStat> sorted =
                        readerTopicStats.stream()
                                .sorted(Comparator.comparingDouble(ReaderTopicStat::getScore).reversed())
                                .collect(Collectors.toList());

                double otherScore = 0;
                int index = 0;
                for (ReaderTopicStat stat : sorted) {
                    if (index < MAX_SLICE) {
                        readerTopicsPieChart.getData().add(
                                new PieChart.Data(stat.getTopicName(), stat.getScore())
                        );
                    } else {
                        otherScore += stat.getScore();
                    }
                    index++;
                }
                if (otherScore > 0) {
                    readerTopicsPieChart.getData().add(
                            new PieChart.Data("Другое", otherScore)
                    );
                }
            }

            messageLabel.setStyle("-fx-text-fill: gray;");
            messageLabel.setText("Статистика для аккаунта: " + currentUser.getUsername());

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Ошибка при загрузке статистики (см. лог).");
        }
    }

    @FXML
    private void handleBackToReader() {
        if (mainController != null) {
            mainController.openReaderPage();
        }
    }
}
