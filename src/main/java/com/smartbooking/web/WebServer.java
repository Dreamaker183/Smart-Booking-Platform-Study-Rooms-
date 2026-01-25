package com.smartbooking.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartbooking.domain.Booking;
import com.smartbooking.domain.Timeslot;
import com.smartbooking.domain.User;
import com.smartbooking.service.AppBootstrap;
import com.smartbooking.service.AppServices;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.json.JavalinJackson;

import java.time.LocalDateTime;

public class WebServer {

    private static AppServices services;

    public static void main(String[] args) {
        services = AppBootstrap.initialize();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper, false));
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        }).start(8080);

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

        // Audit
        app.get("/api/audit", WebServer::handleListAuditLogs);
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
        long userId = Long.parseLong(ctx.queryParam("userId"));
        ctx.json(services.getBookingService().listUserBookings(userId));
    }

    private static void handleListPendingBookings(Context ctx) {
        ctx.json(services.getBookingService().listPendingBookings());
    }

    private static void handleCreateBooking(Context ctx) {
        CreateBookingRequest req = ctx.bodyAsClass(CreateBookingRequest.class);
        Booking booking = services.getBookingService().createBooking(
                req.userId, req.resourceId, new Timeslot(req.start, req.end));
        ctx.json(booking);
    }

    private static void handleApproveBooking(Context ctx) {
        long bookingId = Long.parseLong(ctx.pathParam("id"));
        long adminId = Long.parseLong(ctx.queryParam("adminId"));
        services.getBookingService().approveBooking(adminId, bookingId);
        ctx.status(200);
    }

    private static void handleRejectBooking(Context ctx) {
        long bookingId = Long.parseLong(ctx.pathParam("id"));
        long adminId = Long.parseLong(ctx.queryParam("adminId"));
        services.getBookingService().rejectBooking(adminId, bookingId);
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
            ctx.json(services.getBookingService().listBookingsForResource(resourceId, start, end));
        } else {
            ctx.status(501);
        }
    }

    // --- DTOs ---

    private static class AuthRequest {
        public String username;
        public String password;
    }

    private static class CreateBookingRequest {
        public long userId;
        public long resourceId;
        public LocalDateTime start;
        public LocalDateTime end;
    }

    private static class PayRequest {
        public long userId;
        public String method;
    }
}
