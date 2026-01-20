package com.smartbooking;

import com.smartbooking.domain.Resource;
import com.smartbooking.domain.Timeslot;
import com.smartbooking.persistence.*;
import com.smartbooking.service.*;
import com.smartbooking.domain.state.BookingStateFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConflictDetectionTests {
    private Database database;
    private Connection keepAlive;
    private BookingService bookingService;
    private ResourceRepository resourceRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setup() throws Exception {
        database = new Database("jdbc:sqlite:file:memdb1?mode=memory&cache=shared");
        keepAlive = database.getConnection();
        new DatabaseInitializer().initialize(database);

        UserRepository userRepo = new UserRepository(database);
        ResourceRepository resourceRepo = new ResourceRepository(database);
        BookingRepository bookingRepo = new BookingRepository(database);
        PaymentRepository paymentRepo = new PaymentRepository(database);
        NotificationRepository notificationRepo = new NotificationRepository(database);
        AuditLogRepository auditRepo = new AuditLogRepository(database);

        bookingService = new BookingService(
                bookingRepo,
                resourceRepo,
                new PolicyFactory(),
                new BookingFactory(),
                new BookingStateFactory(),
                new NotificationService(notificationRepo),
                new PaymentService(paymentRepo),
                new AuditService(auditRepo)
        );
        resourceRepository = resourceRepo;
        userRepository = userRepo;
    }

    @AfterEach
    void tearDown() throws Exception {
        keepAlive.close();
    }

    @Test
    void overlappingBookingsAreRejected() {
        long userId = userRepository.findByUsername("alice").orElseThrow().getId();
        Resource resource = resourceRepository.findAll().get(0);
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(2);
        bookingService.createBooking(userId, resource.getId(), new Timeslot(start, end));

        LocalDateTime overlapStart = start.plusMinutes(30);
        LocalDateTime overlapEnd = overlapStart.plusHours(1);
        assertThrows(IllegalStateException.class, () ->
                bookingService.createBooking(userId, resource.getId(), new Timeslot(overlapStart, overlapEnd)));
    }
}
