package com.smartbooking;

import com.smartbooking.domain.Resource;
import com.smartbooking.domain.ResourceType;
import com.smartbooking.domain.Timeslot;
import com.smartbooking.domain.policy.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PolicyTests {
    @Test
    void peakHoursPricingAppliesMultiplier() {
        PricingPolicy policy = new PeakHoursPricingPolicy(1.2, LocalTime.of(18, 0), LocalTime.of(22, 0));
        Resource resource = new Resource(1, "Room", ResourceType.STUDY_ROOM_SMALL, 10.0,
                "PEAK_HOURS", "FLEXIBLE", "AUTO");
        Timeslot slot = new Timeslot(LocalDateTime.now().withHour(19).withMinute(0),
                LocalDateTime.now().withHour(20).withMinute(0));
        double price = policy.calculatePrice(resource, slot, 10.0);
        assertEquals(12.0, price, 0.01);
    }

    @Test
    void weekendPricingAppliesMultiplier() {
        PricingPolicy policy = new WeekendPricingPolicy(1.15);
        Resource resource = new Resource(1, "Room", ResourceType.STUDY_ROOM_SMALL, 10.0,
                "WEEKEND", "FLEXIBLE", "AUTO");
        LocalDateTime saturday = LocalDateTime.now().with(java.time.DayOfWeek.SATURDAY);
        Timeslot slot = new Timeslot(saturday.withHour(10), saturday.withHour(11));
        double price = policy.calculatePrice(resource, slot, 10.0);
        assertEquals(11.5, price, 0.01);
    }

    @Test
    void flexibleCancellationRefundsByHours() {
        CancellationPolicy policy = new FlexibleCancellationPolicy();
        assertEquals(1.0, policy.refundPercent(30), 0.01);
        assertEquals(0.5, policy.refundPercent(4), 0.01);
        assertEquals(0.0, policy.refundPercent(1), 0.01);
    }

    @Test
    void strictCancellationOnlyRefundsAfterThreshold() {
        CancellationPolicy policy = new StrictCancellationPolicy();
        assertEquals(0.8, policy.refundPercent(80), 0.01);
        assertEquals(0.0, policy.refundPercent(10), 0.01);
    }
}
