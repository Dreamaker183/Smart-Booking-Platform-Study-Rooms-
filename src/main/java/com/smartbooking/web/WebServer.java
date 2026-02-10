package com.smartbooking.web;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartbooking.domain.Booking;
import com.smartbooking.domain.Timeslot;
import com.smartbooking.domain.Role;
import com.smartbooking.domain.User;
import com.smartbooking.service.AppBootstrap;
import com.smartbooking.service.AppServices;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.json.JavalinJackson;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import java.util.stream.Collectors;
import java.util.List;

import java.time.LocalDateTime;

public class WebServer {

    private static AppServices services;

    public static void start(AppServices appServices, int port) {
        services = appServices;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper, false));
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        }).start(port);

        // Security Filter
        app.before("/api/*", ctx -> {
            String path = ctx.path();
            if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
                return;
            }

            String userIdStr = ctx.header("X-User-Id");
            if (userIdStr == null || userIdStr.isBlank()) {
                throw new UnauthorizedResponse("Missing authentication header");
            }

            try {
                long userId = Long.parseLong(userIdStr);
                User user = services.getUserRepository().findById(userId)
                        .orElseThrow(() -> new Exception("User not found"));
                ctx.attribute("user", user);
            } catch (Exception e) {
                throw new UnauthorizedResponse("Invalid session");
            }
        });

        // Auth
        app.post("/api/auth/register", WebServer::handleRegister);
        app.post("/api/auth/login", WebServer::handleLogin);

        // Resources
        app.get("/api/resources", WebServer::handleListResources);

        // Bookings
        app.get("/api/bookings", WebServer::handleListBookings);
        app.get("/api/bookings/my", WebServer::handleListMyBookings);
        app.get("/api/bookings/pending", WebServer::handleListPendingBookings);
        app.post("/api/bookings", WebServer::handleCreateBooking);

        app.post("/api/bookings/{id}/approve", WebServer::handleApproveBooking);
        app.post("/api/bookings/{id}/reject", WebServer::handleRejectBooking);
        app.post("/api/bookings/{id}/pay", WebServer::handlePayBooking);
        app.post("/api/bookings/{id}/cancel", WebServer::handleCancelBooking);

        // Admin Actions
        app.post("/api/admin/bookings/{id}/update", ctx -> {
            checkAdmin(ctx);
            WebServer.handleUpdateBooking(ctx);
        });
        app.post("/api/admin/bookings/{id}/delete", ctx -> {
            checkAdmin(ctx);
            WebServer.handleDeleteBooking(ctx);
        });

        // Secured Audit
        app.get("/api/audit", ctx -> {
            checkAdmin(ctx);
            WebServer.handleListAuditLogs(ctx);
        });
    }

    private static void checkAdmin(Context ctx) {
        User user = ctx.attribute("user");
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new ForbiddenResponse("Admin access required");
        }
    }

    public static void main(String[] args) {
        AppServices services = AppBootstrap.initialize();
        start(services, 8080);
    }

    // --- Handlers ---

    private static void handleRegister(Context ctx) {
        AuthRequest req = ctx.bodyAsClass(AuthRequest.class);
        User user = services.getAuthService().register(req.username, req.password);
        ctx.json(user);
    }

    private static void handleLogin(Context ctx) {
        AuthRequest req = ctx.bodyAsClass(AuthRequest.class);
        User user = services.getAuthService().login(req.username, req.password);
        ctx.json(user);
    }

    private static void handleListResources(Context ctx) {
        ctx.json(services.getResourceService().listResources());
    }

    private static void handleListMyBookings(Context ctx) {
        User user = ctx.attribute("user");
        ctx.json(services.getBookingService().listUserBookings(user.getId()));
    }

    private static void handleListPendingBookings(Context ctx) {
        checkAdmin(ctx);
        ctx.json(services.getBookingService().listPendingBookings());
    }

    private static void handleCreateBooking(Context ctx) {
        User user = ctx.attribute("user");
        CreateBookingRequest req = ctx.bodyAsClass(CreateBookingRequest.class);
        Booking booking = services.getBookingService().createBooking(
                user.getId(), req.resourceId, new Timeslot(req.start, req.end));
        ctx.json(booking);
    }

    private static void handleApproveBooking(Context ctx) {
        long bookingId = Long.parseLong(ctx.pathParam("id"));
        User admin = ctx.attribute("user");
        checkAdmin(ctx);
        services.getBookingService().approveBooking(admin.getId(), bookingId);
        ctx.status(200);
    }

    private static void handleRejectBooking(Context ctx) {
        long bookingId = Long.parseLong(ctx.pathParam("id"));
        User admin = ctx.attribute("user");
        checkAdmin(ctx);
        services.getBookingService().rejectBooking(admin.getId(), bookingId);
        ctx.status(200);
    }

    private static void handlePayBooking(Context ctx) {
        long bookingId = Long.parseLong(ctx.pathParam("id"));
        PayRequest req = ctx.bodyAsClass(PayRequest.class);
        services.getBookingService().payBooking(req.userId, bookingId, req.method);
        ctx.status(200);
    }

    private static void handleCancelBooking(Context ctx) {
        long bookingId = Long.parseLong(ctx.pathParam("id"));
        long userId = Long.parseLong(ctx.queryParam("userId"));
        services.getBookingService().cancelBooking(userId, bookingId);
        ctx.status(200);
    }

    private static void handleListAuditLogs(Context ctx) {
        ctx.json(services.getAuditService().listLogs());
    }

    private static void handleListBookings(Context ctx) {
        String resourceIdStr = ctx.queryParam("resourceId");
        String startStr = ctx.queryParam("start");
        String endStr = ctx.queryParam("end");

        if (resourceIdStr != null && startStr != null && endStr != null) {
            long resourceId = Long.parseLong(resourceIdStr);
            LocalDateTime start = LocalDateTime.parse(startStr);
            LocalDateTime end = LocalDateTime.parse(endStr);

            List<Booking> bookings = services.getBookingService().listBookingsForResource(resourceId, start, end);

            // Data Privacy: Redact usernames for non-admin users
            User currentUser = ctx.attribute("user");
            if (currentUser.getRole() != Role.ADMIN) {
                bookings = bookings.stream().map(b -> {
                    if (b.getUserId() != currentUser.getId()) {
                        return new Booking(b.getId(), b.getUserId(), "Occupied", b.getResourceId(),
                                b.getStartTime(), b.getEndTime(), b.getPrice(), b.getStatus(), b.getCreatedAt());
                    }
                    return b;
                }).collect(Collectors.toList());
            }

            ctx.json(bookings);
        } else {
            ctx.status(501);
        }
    }

    private static void handleUpdateBooking(Context ctx) {
        long bookingId = Long.parseLong(ctx.pathParam("id"));
        User admin = ctx.attribute("user");
        // checkAdmin is already called in the lambda route
        UpdateBookingRequest req = ctx.bodyAsClass(UpdateBookingRequest.class);
        services.getBookingService().updateBooking(admin.getId(), bookingId, req.start, req.end);
        ctx.status(200);
    }

    private static void handleDeleteBooking(Context ctx) {
        long bookingId = Long.parseLong(ctx.pathParam("id"));
        User admin = ctx.attribute("user");
        // checkAdmin is already called in the lambda route
        services.getBookingService().deleteBooking(admin.getId(), bookingId);
        ctx.status(200);
    }

    // --- DTOs ---

    private static class AuthRequest {
        public String username;
        public String password;
    }

    private static class CreateBookingRequest {
        public long resourceId;
        public LocalDateTime start;
        public LocalDateTime end;
    }

    private static class PayRequest {
        public long userId;
        public String method;
    }

    private static class UpdateBookingRequest {
        public LocalDateTime start;
        public LocalDateTime end;
    }
}
