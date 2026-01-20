package com.smartbooking.service;

import com.smartbooking.domain.policy.*;

import java.time.LocalTime;
import java.util.List;

public class PolicyFactory {
    public PricingPolicy createPricingPolicy(String key) {
        if ("PEAK_HOURS".equalsIgnoreCase(key)) {
            return new PeakHoursPricingPolicy(1.2, LocalTime.of(18, 0), LocalTime.of(22, 0));
        }
        if ("WEEKEND".equalsIgnoreCase(key)) {
            return new WeekendPricingPolicy(1.15);
        }
        if ("PEAK_WEEKEND".equalsIgnoreCase(key)) {
            return new CompositePricingPolicy(List.of(
                    new PeakHoursPricingPolicy(1.2, LocalTime.of(18, 0), LocalTime.of(22, 0)),
                    new WeekendPricingPolicy(1.15)
            ));
        }
        return new DefaultPricingPolicy();
    }

    public CancellationPolicy createCancellationPolicy(String key) {
        if ("STRICT".equalsIgnoreCase(key)) {
            return new StrictCancellationPolicy();
        }
        return new FlexibleCancellationPolicy();
    }

    public ApprovalPolicy createApprovalPolicy(String key) {
        if ("ADMIN_REQUIRED".equalsIgnoreCase(key)) {
            return new AdminApprovalPolicy();
        }
        return new AutoApprovalPolicy();
    }
}
