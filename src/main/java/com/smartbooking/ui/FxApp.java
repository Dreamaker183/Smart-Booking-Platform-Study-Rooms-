package com.smartbooking.ui;

import com.smartbooking.domain.*;
import com.smartbooking.service.*;
import com.smartbooking.util.DateTimeUtil;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

public class FxApp extends Application {
    private static AppServices services;
    private Stage stage;
    private User currentUser;

    public static void setServices(AppServices services) {
        FxApp.services = services;
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Smart Booking Platform");
        Scene scene = createLoginScene();
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private Scene createLoginScene() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(40));
        container.getStyleClass().add("card");

        VBox header = new VBox(5);
        Label titleLabel = new Label("Smart Booking");
        titleLabel.getStyleClass().add("section-title");
        Label subtitle = new Label("Sign in to manage your sessions");
        subtitle.getStyleClass().add("subtitle");
        header.getChildren().addAll(titleLabel, subtitle);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        TextField username = new TextField();
        username.setPromptText("Enter username");
        PasswordField password = new PasswordField();
        password.setPromptText("Enter password");
        Label status = new Label();

        grid.addRow(0, new Label("Username"), username);
        grid.addRow(1, new Label("Password"), password);

        HBox buttons = new HBox(15, new Button("Login"), new Button("Register"));
        buttons.getChildren().get(1).getStyleClass().add("secondary");

        VBox loginBox = new VBox(20, header, grid, buttons, status);

        Button loginBtn = (Button) buttons.getChildren().get(0);
        Button regBtn = (Button) buttons.getChildren().get(1);

        loginBtn.setOnAction(event -> {
            try {
                currentUser = services.getAuthService().login(username.getText().trim(), password.getText());
                if (currentUser.getRole() == Role.ADMIN) {
                    stage.setScene(createAdminScene());
                } else {
                    stage.setScene(createCustomerScene());
                }
            } catch (Exception ex) {
                status.setText("Login failed: " + ex.getMessage());
            }
        });

        regBtn.setOnAction(event -> {
            try {
                services.getAuthService().register(username.getText().trim(), password.getText());
                status.setText("Registered! You can now login.");
            } catch (Exception ex) {
                status.setText("Register failed: " + ex.getMessage());
            }
        });

        Scene scene = new Scene(new VBox(loginBox), 520, 380);
        scene.getRoot().getStyleClass().add("screen");
        ((VBox) scene.getRoot()).setPadding(new Insets(30));
        return scene;
    }

    private Scene createCustomerScene() {
        VBox header = new VBox(5);
        Label titleLabel = new Label("Dashboard");
        titleLabel.getStyleClass().add("section-title");
        Label subtitle = new Label("View room availability and create bookings");
        subtitle.getStyleClass().add("subtitle");
        header.getChildren().addAll(titleLabel, subtitle);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Dashboard", createDashboardPane()));
        tabs.getTabs().add(new Tab("My History", createMyBookingsPane()));
        tabs.getTabs().add(new Tab("Notifications", createNotificationsPane()));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Button logout = new Button("Sign Out");
        logout.getStyleClass().add("secondary");
        logout.setOnAction(event -> {
            currentUser = null;
            stage.setScene(createLoginScene());
        });

        HBox topBar = new HBox(20, header);
        topBar.setPadding(new Insets(0, 0, 10, 0));

        VBox root = new VBox(20, topBar, tabs, logout);
        root.setPadding(new Insets(30));
        Scene scene = new Scene(root, 1000, 700);
        scene.getRoot().getStyleClass().add("screen");
        return scene;
    }

    private Scene createAdminScene() {
        VBox header = new VBox(5);
        Label titleLabel = new Label("Admin Center");
        titleLabel.getStyleClass().add("section-title");
        Label subtitle = new Label("System overview and approval requests");
        subtitle.getStyleClass().add("subtitle");
        header.getChildren().addAll(titleLabel, subtitle);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Approvals", createPendingPane()));
        tabs.getTabs().add(new Tab("System Logs", createAuditPane()));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Button logout = new Button("Sign Out");
        logout.getStyleClass().add("secondary");
        logout.setOnAction(event -> {
            currentUser = null;
            stage.setScene(createLoginScene());
        });

        VBox root = new VBox(20, header, tabs, logout);
        root.setPadding(new Insets(30));
        Scene scene = new Scene(root, 1000, 700);
        scene.getRoot().getStyleClass().add("screen");
        return scene;
    }

    private VBox createDashboardPane() {
        ComboBox<Resource> resourceBox = new ComboBox<>();
        resourceBox.setItems(FXCollections.observableArrayList(services.getResourceService().listResources()));
        resourceBox.setPromptText("Select a Room to View Availability");
        resourceBox.setMaxWidth(Double.MAX_VALUE);

        TimetablePane timetable = new TimetablePane(services, currentUser.getId());

        resourceBox.setOnAction(e -> {
            Resource r = resourceBox.getValue();
            if (r != null) {
                timetable.setResource(r.getId());
            }
        });

        // Booking Form Section
        VBox bookingForm = new VBox(20);
        bookingForm.setPadding(new Insets(20));
        bookingForm.getStyleClass().add("card");
        bookingForm.setMinWidth(300);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<LocalTime> startTime = new ComboBox<>(buildTimeOptions());
        ComboBox<LocalTime> endTime = new ComboBox<>(buildTimeOptions());
        timetable.setSelectionListener((date, start, end) -> {
            datePicker.setValue(date);
            startTime.setValue(start);
            endTime.setValue(end);
        });

        Label status = new Label();
        status.setWrapText(true);
        Button bookBtn = new Button("Confirm Booking");
        bookBtn.setMaxWidth(Double.MAX_VALUE);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.addRow(0, new Label("Date"), datePicker);
        formGrid.addRow(1, new Label("Start"), startTime);
        formGrid.addRow(2, new Label("End"), endTime);

        bookBtn.setOnAction(e -> {
            try {
                Resource r = resourceBox.getValue();
                if (r == null)
                    throw new IllegalArgumentException("Select a room first");
                LocalDateTime start = LocalDateTime.of(datePicker.getValue(), startTime.getValue());
                LocalDateTime end = LocalDateTime.of(datePicker.getValue(), endTime.getValue());
                services.getBookingService().createBooking(currentUser.getId(), r.getId(), new Timeslot(start, end));
                status.setText("Booking successful!");
                timetable.refresh();
            } catch (Exception ex) {
                status.setText("Error: " + ex.getMessage());
            }
        });

        bookingForm.getChildren().addAll(header("Quick Booking"), formGrid, bookBtn, status);

        HBox mainLayout = new HBox(20, timetable, bookingForm);
        HBox.setHgrow(timetable, Priority.ALWAYS);

        VBox content = new VBox(20, resourceBox, mainLayout);
        return content;
    }

    private VBox createMyBookingsPane() {
        ListView<String> list = new ListView<>();
        list.setPrefHeight(350);
        Button refresh = new Button("Refresh History");
        refresh.getStyleClass().add("secondary");

        TextField bookingId = new TextField();
        bookingId.setPromptText("ID");
        bookingId.setPrefWidth(60);

        TextField method = new TextField();
        method.setPromptText("CARD/CASH");

        Button pay = new Button("Pay Now");
        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("secondary");
        Label status = new Label();

        refresh.setOnAction(event -> list.setItems(loadBookings()));
        list.setItems(loadBookings());

        pay.setOnAction(event -> {
            try {
                long id = Long.parseLong(bookingId.getText().trim());
                services.getBookingService().payBooking(currentUser.getId(), id, method.getText().trim());
                status.setText("Payment successful.");
                list.setItems(loadBookings());
            } catch (Exception ex) {
                status.setText("Pay failed: " + ex.getMessage());
            }
        });

        cancel.setOnAction(event -> {
            try {
                long id = Long.parseLong(bookingId.getText().trim());
                services.getBookingService().cancelBooking(currentUser.getId(), id);
                status.setText("Booking cancelled.");
                list.setItems(loadBookings());
            } catch (Exception ex) {
                status.setText("Error: " + ex.getMessage());
            }
        });

        HBox actions = new HBox(15, new Label("Action on ID:"), bookingId, method, pay, cancel);
        actions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox content = new VBox(20, header("My Booking History"), refresh, list, actions, status);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card");
        return content;
    }

    private VBox createNotificationsPane() {
        ListView<String> list = new ListView<>();
        Button refresh = new Button("Refresh");
        refresh.setOnAction(event -> list.setItems(loadNotifications()));
        list.setItems(loadNotifications());
        VBox root = new VBox(10, header("Notifications"), refresh, list);
        root.setPadding(new Insets(10));
        return root;
    }

    private VBox createPendingPane() {
        ListView<String> list = new ListView<>();
        list.setPrefHeight(400);
        Button refresh = new Button("Fetch Requests");
        refresh.getStyleClass().add("secondary");

        TextField bookingId = new TextField();
        bookingId.setPromptText("Booking ID");
        Button approve = new Button("Approve");
        Button reject = new Button("Reject");
        reject.getStyleClass().add("secondary");
        Label status = new Label();

        refresh.setOnAction(event -> list.setItems(loadPending()));
        list.setItems(loadPending());

        approve.setOnAction(event -> {
            try {
                long id = Long.parseLong(bookingId.getText().trim());
                services.getBookingService().approveBooking(currentUser.getId(), id);
                status.setText("Booking approved.");
                list.setItems(loadPending());
            } catch (Exception ex) {
                status.setText("Approve failed: " + ex.getMessage());
            }
        });

        reject.setOnAction(event -> {
            try {
                long id = Long.parseLong(bookingId.getText().trim());
                services.getBookingService().rejectBooking(currentUser.getId(), id);
                status.setText("Booking rejected.");
                list.setItems(loadPending());
            } catch (Exception ex) {
                status.setText("Reject failed: " + ex.getMessage());
            }
        });

        HBox actions = new HBox(15, new Label("Process ID:"), bookingId, approve, reject);
        actions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox content = new VBox(20, header("Awaiting Your Approval"), refresh, list, actions, status);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card");
        return content;
    }

    private VBox createAuditPane() {
        ListView<String> list = new ListView<>();
        list.setPrefHeight(450);
        Button refresh = new Button("Update Logs");
        refresh.getStyleClass().add("secondary");
        refresh.setOnAction(event -> list.setItems(loadAudit()));
        list.setItems(loadAudit());

        VBox content = new VBox(20, header("System Activity Log"), refresh, list);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card");
        return content;
    }

    private Label header(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    private ObservableList<String> loadBookings() {
        List<Booking> bookings = services.getBookingService().listUserBookings(currentUser.getId());
        return FXCollections.observableArrayList(bookings.stream()
                .map(booking -> String.format("%d - User: %s | Room: %d | %s -> %s | %s | $%.2f",
                        booking.getId(), booking.getUsername() != null ? booking.getUsername() : "Me",
                        booking.getResourceId(),
                        DateTimeUtil.format(booking.getStartTime()), DateTimeUtil.format(booking.getEndTime()),
                        booking.getStatus(), booking.getPrice()))
                .toList());
    }

    private ObservableList<String> loadNotifications() {
        List<Notification> notifications = services.getNotificationService().getNotifications(currentUser.getId());
        return FXCollections.observableArrayList(notifications.stream()
                .map(notification -> DateTimeUtil.format(notification.getCreatedAt()) + " - "
                        + notification.getMessage())
                .toList());
    }

    private ObservableList<String> loadPending() {
        List<Booking> bookings = services.getBookingService().listPendingBookings();
        return FXCollections.observableArrayList(bookings.stream()
                .map(booking -> String.format("%d - User: %s (%d) | Room: %d | %s -> %s",
                        booking.getId(), booking.getUsername(), booking.getUserId(), booking.getResourceId(),
                        DateTimeUtil.format(booking.getStartTime()), DateTimeUtil.format(booking.getEndTime())))
                .toList());
    }

    private ObservableList<String> loadAudit() {
        List<AuditLog> logs = services.getAuditService().listLogs();
        return FXCollections.observableArrayList(logs.stream()
                .map(log -> DateTimeUtil.format(log.getCreatedAt()) + " | User " + log.getUserId() + " | "
                        + log.getAction() + " | " + log.getDetails())
                .toList());
    }

    private ObservableList<LocalTime> buildTimeOptions() {
        return FXCollections.observableArrayList(IntStream.range(8, 22)
                .mapToObj(hour -> LocalTime.of(hour, 0))
                .toList());
    }

}
