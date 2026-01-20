package com.smartbooking.ui;

import com.smartbooking.domain.*;
import com.smartbooking.service.*;
import com.smartbooking.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private final AuthService authService;
    private final ResourceService resourceService;
    private final BookingService bookingService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleUI(AuthService authService,
                     ResourceService resourceService,
                     BookingService bookingService,
                     NotificationService notificationService,
                     AuditService auditService) {
        this.authService = authService;
        this.resourceService = resourceService;
        this.bookingService = bookingService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== Smart Booking Platform ===");
            System.out.println("1) Login");
            System.out.println("2) Register");
            System.out.println("0) Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            if ("1".equals(choice)) {
                handleLogin();
            } else if ("2".equals(choice)) {
                handleRegister();
            } else if ("0".equals(choice)) {
                System.out.println("Goodbye!");
                return;
            }
        }
    }

    private void handleLogin() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            User user = authService.login(username, password);
            if (user.getRole() == Role.ADMIN) {
                adminMenu(user);
            } else {
                customerMenu(user);
            }
        } catch (Exception ex) {
            System.out.println("Login failed: " + ex.getMessage());
        }
    }

    private void handleRegister() {
        try {
            System.out.print("Choose username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Choose password: ");
            String password = scanner.nextLine().trim();
            authService.register(username, password);
            System.out.println("Registration successful. You can now login.");
        } catch (Exception ex) {
            System.out.println("Registration failed: " + ex.getMessage());
        }
    }

    private void customerMenu(User user) {
        while (true) {
            System.out.println("\n=== Customer Dashboard ===");
            System.out.println("1) Browse resources");
            System.out.println("2) Create booking");
            System.out.println("3) My bookings");
            System.out.println("4) View notifications");
            System.out.println("0) Logout");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> listResources();
                case "2" -> createBooking(user);
                case "3" -> listBookings(user);
                case "4" -> viewNotifications(user);
                case "0" -> {return;}
                default -> System.out.println("Invalid option");
            }
        }
    }

    private void adminMenu(User user) {
        while (true) {
            System.out.println("\n=== Admin Dashboard ===");
            System.out.println("1) Pending approvals");
            System.out.println("2) Approve booking");
            System.out.println("3) Reject booking");
            System.out.println("4) View audit log");
            System.out.println("0) Logout");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> listPendingBookings();
                case "2" -> approveBooking(user);
                case "3" -> rejectBooking(user);
                case "4" -> viewAuditLog();
                case "0" -> {return;}
                default -> System.out.println("Invalid option");
            }
        }
    }

    private void listResources() {
        List<Resource> resources = resourceService.listResources();
        System.out.println("\n--- Resources ---");
        for (Resource resource : resources) {
            System.out.printf("%d) %s [%s] $%.2f/hr Policy: %s/%s/%s%n",
                    resource.getId(), resource.getName(), resource.getType(), resource.getBasePricePerHour(),
                    resource.getPricingPolicyKey(), resource.getCancellationPolicyKey(), resource.getApprovalPolicyKey());
        }
    }

    private void createBooking(User user) {
        try {
            listResources();
            System.out.print("Resource ID: ");
            long resourceId = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Start time (yyyy-MM-dd HH:mm): ");
            LocalDateTime start = DateTimeUtil.parse(scanner.nextLine().trim());
            System.out.print("End time (yyyy-MM-dd HH:mm): ");
            LocalDateTime end = DateTimeUtil.parse(scanner.nextLine().trim());
            Booking booking = bookingService.createBooking(user.getId(), resourceId, new Timeslot(start, end));
            System.out.printf("Booking %d created. Status: %s, Price: $%.2f%n",
                    booking.getId(), booking.getStatus(), booking.getPrice());
            if (booking.getStatus() == BookingStatus.APPROVED) {
                handlePayment(user, booking);
            }
        } catch (Exception ex) {
            System.out.println("Booking failed: " + ex.getMessage());
        }
    }

    private void handlePayment(User user, Booking booking) {
        System.out.print("Pay now? (y/n): ");
        String choice = scanner.nextLine().trim();
        if ("y".equalsIgnoreCase(choice)) {
            System.out.print("Payment method (CARD/CASH): ");
            String method = scanner.nextLine().trim();
            bookingService.payBooking(user.getId(), booking.getId(), method);
            System.out.println("Payment successful.");
        }
    }

    private void listBookings(User user) {
        List<Booking> bookings = bookingService.listUserBookings(user.getId());
        System.out.println("\n--- My Bookings ---");
        for (Booking booking : bookings) {
            System.out.printf("%d) Resource %d %s -> %s | %s | $%.2f%n",
                    booking.getId(), booking.getResourceId(),
                    DateTimeUtil.format(booking.getStartTime()), DateTimeUtil.format(booking.getEndTime()),
                    booking.getStatus(), booking.getPrice());
        }
        if (!bookings.isEmpty()) {
            System.out.print("Cancel a booking? (id or blank): ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                long bookingId = Long.parseLong(input);
                bookingService.cancelBooking(user.getId(), bookingId);
                System.out.println("Booking updated.");
            }
        }
    }

    private void viewNotifications(User user) {
        List<Notification> notifications = notificationService.getNotifications(user.getId());
        System.out.println("\n--- Notifications ---");
        for (Notification notification : notifications) {
            System.out.printf("%s - %s%n", DateTimeUtil.format(notification.getCreatedAt()), notification.getMessage());
        }
    }

    private void listPendingBookings() {
        List<Booking> bookings = bookingService.listPendingBookings();
        System.out.println("\n--- Pending Bookings ---");
        for (Booking booking : bookings) {
            System.out.printf("%d) User %d Resource %d %s -> %s | %s%n",
                    booking.getId(), booking.getUserId(), booking.getResourceId(),
                    DateTimeUtil.format(booking.getStartTime()), DateTimeUtil.format(booking.getEndTime()),
                    booking.getStatus());
        }
    }

    private void approveBooking(User admin) {
        try {
            System.out.print("Booking ID to approve: ");
            long bookingId = Long.parseLong(scanner.nextLine().trim());
            bookingService.approveBooking(admin.getId(), bookingId);
            System.out.println("Booking approved.");
        } catch (Exception ex) {
            System.out.println("Approve failed: " + ex.getMessage());
        }
    }

    private void rejectBooking(User admin) {
        try {
            System.out.print("Booking ID to reject: ");
            long bookingId = Long.parseLong(scanner.nextLine().trim());
            bookingService.rejectBooking(admin.getId(), bookingId);
            System.out.println("Booking rejected.");
        } catch (Exception ex) {
            System.out.println("Reject failed: " + ex.getMessage());
        }
    }

    private void viewAuditLog() {
        System.out.println("\n--- Audit Log ---");
        auditService.listLogs().forEach(log ->
                System.out.printf("%s | User %d | %s | %s%n",
                        DateTimeUtil.format(log.getCreatedAt()), log.getUserId(), log.getAction(), log.getDetails()));
    }
}
