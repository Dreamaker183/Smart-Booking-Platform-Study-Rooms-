package com.smartbooking.service;

public class AppServices {
    private final AuthService authService;
    private final ResourceService resourceService;
    private final BookingService bookingService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public AppServices(AuthService authService,
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

    public AuthService getAuthService() {
        return authService;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    public BookingService getBookingService() {
        return bookingService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public AuditService getAuditService() {
        return auditService;
    }
}
