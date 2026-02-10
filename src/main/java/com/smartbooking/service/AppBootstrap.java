package com.smartbooking.service;

import com.smartbooking.domain.state.BookingStateFactory;
import com.smartbooking.persistence.*;

public class AppBootstrap {
    public static AppServices initialize() {
        Database database = new Database("jdbc:postgresql://localhost:5432/smart_booking", "postgres", "postgres");
        new DatabaseInitializer().initialize(database);

        UserRepository userRepository = new UserRepository(database);
        ResourceRepository resourceRepository = new ResourceRepository(database);
        BookingRepository bookingRepository = new BookingRepository(database);
        PaymentRepository paymentRepository = new PaymentRepository(database);
        NotificationRepository notificationRepository = new NotificationRepository(database);
        AuditLogRepository auditLogRepository = new AuditLogRepository(database);

        PolicyFactory policyFactory = new PolicyFactory();
        BookingFactory bookingFactory = new BookingFactory();
        BookingStateFactory stateFactory = new BookingStateFactory();
        NotificationService notificationService = new NotificationService(notificationRepository);
        PaymentService paymentService = new PaymentService(paymentRepository);
        AuditService auditService = new AuditService(auditLogRepository);

        AuthService authService = new AuthService(userRepository);
        ResourceService resourceService = new ResourceService(resourceRepository);
        BookingService bookingService = new BookingService(
                bookingRepository,
                resourceRepository,
                policyFactory,
                bookingFactory,
                stateFactory,
                notificationService,
                paymentService,
                auditService);

        return new AppServices(userRepository, authService, resourceService, bookingService, notificationService,
                auditService);
    }
}
