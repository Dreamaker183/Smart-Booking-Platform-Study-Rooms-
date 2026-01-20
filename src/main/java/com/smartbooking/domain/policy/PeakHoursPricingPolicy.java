package com.smartbooking.domain.policy;

import com.smartbooking.domain.Resource;
import com.smartbooking.domain.Timeslot;

import java.time.LocalTime;

public class PeakHoursPricingPolicy implements PricingPolicy {
    private final double multiplier;
    private final LocalTime peakStart;
    private final LocalTime peakEnd;

    public PeakHoursPricingPolicy(double multiplier, LocalTime peakStart, LocalTime peakEnd) {
        this.multiplier = multiplier;
        this.peakStart = peakStart;
        this.peakEnd = peakEnd;
    }

    @Override
    public double calculatePrice(Resource resource, Timeslot timeslot, double basePrice) {
        LocalTime start = timeslot.getStart().toLocalTime();
        if (!start.isBefore(peakStart) && start.isBefore(peakEnd)) {
            return basePrice * multiplier;
        }
        return basePrice;
    }

    @Override
    public String getName() {
        return "PEAK_HOURS";
    }
}
