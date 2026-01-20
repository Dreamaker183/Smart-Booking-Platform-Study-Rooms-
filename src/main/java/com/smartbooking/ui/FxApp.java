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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;

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
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField username = new TextField();
        PasswordField password = new PasswordField();
        Label status = new Label();

        Button login = new Button("Login");
        Button register = new Button("Register");

        grid.addRow(0, new Label("Username:"), username);
        grid.addRow(1, new Label("Password:"), password);
        grid.add(new HBox(10, login, register), 1, 2);
        grid.add(status, 1, 3);

        login.setOnAction(event -> {
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

        register.setOnAction(event -> {
            try {
                services.getAuthService().register(username.getText().trim(), password.getText());
                status.setText("Registered. You can login now.");
            } catch (Exception ex) {
                status.setText("Register failed: " + ex.getMessage());
            }
        });

        Scene scene = new Scene(grid, 480, 260);
        scene.getRoot().getStyleClass().add("screen");
        return scene;
    }

    private Scene createCustomerScene() {
        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Resources", createResourcesPane()));
        tabs.getTabs().add(new Tab("Create Booking", createBookingPane()));
        tabs.getTabs().add(new Tab("My Bookings", createMyBookingsPane()));
        tabs.getTabs().add(new Tab("Notifications", createNotificationsPane()));

        Button logout = new Button("Logout");
        logout.setOnAction(event -> {
            currentUser = null;
            stage.setScene(createLoginScene());
        });

        VBox root = new VBox(10, tabs, logout);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 900, 560);
        scene.getRoot().getStyleClass().add("screen");
        return scene;
    }

    private Scene createAdminScene() {
        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Pending Approvals", createPendingPane()));
        tabs.getTabs().add(new Tab("Audit Log", createAuditPane()));

        Button logout = new Button("Logout");
        logout.setOnAction(event -> {
            currentUser = null;
            stage.setScene(createLoginScene());
        });

        VBox root = new VBox(10, tabs, logout);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 900, 560);
        scene.getRoot().getStyleClass().add("screen");
        return scene;
    }

    private VBox createResourcesPane() {
        ListView<String> list = new ListView<>();
        Button refresh = new Button("Refresh");
        refresh.setOnAction(event -> list.setItems(loadResources()));
        list.setItems(loadResources());
        VBox root = new VBox(10, header("Resources"), refresh, list);
        root.setPadding(new Insets(10));
        return root;
    }

    private VBox createBookingPane() {
        TextField resourceId = new TextField();
        TextField start = new TextField();
        TextField end = new TextField();
        Label status = new Label();
        Button create = new Button("Create Booking");

        start.setPromptText("yyyy-MM-dd HH:mm");
        end.setPromptText("yyyy-MM-dd HH:mm");

        create.setOnAction(event -> {
            try {
                long id = Long.parseLong(resourceId.getText().trim());
                LocalDateTime startTime = DateTimeUtil.parse(start.getText().trim());
                LocalDateTime endTime = DateTimeUtil.parse(end.getText().trim());
                Booking booking = services.getBookingService().createBooking(currentUser.getId(), id, new Timeslot(startTime, endTime));
                status.setText("Created booking " + booking.getId() + " status " + booking.getStatus() + " price $" + String.format("%.2f", booking.getPrice()));
            } catch (Exception ex) {
                status.setText("Create failed: " + ex.getMessage());
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Resource ID:"), resourceId);
        grid.addRow(1, new Label("Start:"), start);
        grid.addRow(2, new Label("End:"), end);
        grid.add(create, 1, 3);

        VBox root = new VBox(10, header("Create a Booking"), grid, status);
        root.setPadding(new Insets(10));
        return root;
    }

    private VBox createMyBookingsPane() {
        ListView<String> list = new ListView<>();
        Button refresh = new Button("Refresh");
        TextField bookingId = new TextField();
        TextField method = new TextField();
        Button pay = new Button("Pay");
        Button cancel = new Button("Cancel");
        Label status = new Label();

        method.setPromptText("CARD or CASH");

        refresh.setOnAction(event -> list.setItems(loadBookings()));
        list.setItems(loadBookings());

        pay.setOnAction(event -> {
            try {
                long id = Long.parseLong(bookingId.getText().trim());
                services.getBookingService().payBooking(currentUser.getId(), id, method.getText().trim());
                status.setText("Payment recorded.");
                list.setItems(loadBookings());
            } catch (Exception ex) {
                status.setText("Pay failed: " + ex.getMessage());
            }
        });

        cancel.setOnAction(event -> {
            try {
                long id = Long.parseLong(bookingId.getText().trim());
                services.getBookingService().cancelBooking(currentUser.getId(), id);
                status.setText("Booking updated.");
                list.setItems(loadBookings());
            } catch (Exception ex) {
                status.setText("Cancel failed: " + ex.getMessage());
            }
        });

        HBox actions = new HBox(10, new Label("Booking ID:"), bookingId, new Label("Method:"), method, pay, cancel);
        VBox root = new VBox(10, header("My Bookings"), refresh, list, actions, status);
        root.setPadding(new Insets(10));
        return root;
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
        Button refresh = new Button("Refresh");
        TextField bookingId = new TextField();
        Button approve = new Button("Approve");
        Button reject = new Button("Reject");
        Label status = new Label();

        refresh.setOnAction(event -> list.setItems(loadPending()));
        list.setItems(loadPending());

        approve.setOnAction(event -> {
            try {
                long id = Long.parseLong(bookingId.getText().trim());
                services.getBookingService().approveBooking(currentUser.getId(), id);
                status.setText("Approved.");
                list.setItems(loadPending());
            } catch (Exception ex) {
                status.setText("Approve failed: " + ex.getMessage());
            }
        });

        reject.setOnAction(event -> {
            try {
                long id = Long.parseLong(bookingId.getText().trim());
                services.getBookingService().rejectBooking(currentUser.getId(), id);
                status.setText("Rejected.");
                list.setItems(loadPending());
            } catch (Exception ex) {
                status.setText("Reject failed: " + ex.getMessage());
            }
        });

        HBox actions = new HBox(10, new Label("Booking ID:"), bookingId, approve, reject);
        VBox root = new VBox(10, header("Pending Approvals"), refresh, list, actions, status);
        root.setPadding(new Insets(10));
        return root;
    }

    private VBox createAuditPane() {
        ListView<String> list = new ListView<>();
        Button refresh = new Button("Refresh");
        refresh.setOnAction(event -> list.setItems(loadAudit()));
        list.setItems(loadAudit());
        VBox root = new VBox(10, header("Audit Log"), refresh, list);
        root.setPadding(new Insets(10));
        return root;
    }

    private Label header(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    private ObservableList<String> loadResources() {
        List<Resource> resources = services.getResourceService().listResources();
        return FXCollections.observableArrayList(resources.stream()
                .map(resource -> String.format("%d - %s (%s) $%.2f/hr Policies: %s/%s/%s",
                        resource.getId(), resource.getName(), resource.getType(), resource.getBasePricePerHour(),
                        resource.getPricingPolicyKey(), resource.getCancellationPolicyKey(), resource.getApprovalPolicyKey()))
                .toList());
    }

    private ObservableList<String> loadBookings() {
        List<Booking> bookings = services.getBookingService().listUserBookings(currentUser.getId());
        return FXCollections.observableArrayList(bookings.stream()
                .map(booking -> String.format("%d - Resource %d %s -> %s | %s | $%.2f",
                        booking.getId(), booking.getResourceId(),
                        DateTimeUtil.format(booking.getStartTime()), DateTimeUtil.format(booking.getEndTime()),
                        booking.getStatus(), booking.getPrice()))
                .toList());
    }

    private ObservableList<String> loadNotifications() {
        List<Notification> notifications = services.getNotificationService().getNotifications(currentUser.getId());
        return FXCollections.observableArrayList(notifications.stream()
                .map(notification -> DateTimeUtil.format(notification.getCreatedAt()) + " - " + notification.getMessage())
                .toList());
    }

    private ObservableList<String> loadPending() {
        List<Booking> bookings = services.getBookingService().listPendingBookings();
        return FXCollections.observableArrayList(bookings.stream()
                .map(booking -> String.format("%d - User %d Resource %d %s -> %s",
                        booking.getId(), booking.getUserId(), booking.getResourceId(),
                        DateTimeUtil.format(booking.getStartTime()), DateTimeUtil.format(booking.getEndTime())))
                .toList());
    }

    private ObservableList<String> loadAudit() {
        List<AuditLog> logs = services.getAuditService().listLogs();
        return FXCollections.observableArrayList(logs.stream()
                .map(log -> DateTimeUtil.format(log.getCreatedAt()) + " | User " + log.getUserId() + " | " + log.getAction() + " | " + log.getDetails())
                .toList());
    }
}
