package com.smartbooking.service;

import com.smartbooking.persistence.UserRepository;

public class AppServices {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final ResourceService resourceService;
    private final BookingService bookingService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public AppServices(UserRepository userRepository,
            AuthService authService,
            ResourceService resourceService,
            BookingService bookingService,
            NotificationService notificationService,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.resourceService = resourceService;
        this.bookingService = bookingService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    public UserRepository getUserRepository() {
        return userRepository;
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
