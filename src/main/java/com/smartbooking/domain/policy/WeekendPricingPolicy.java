package com.smartbooking.domain.policy;

import com.smartbooking.domain.Resource;
import com.smartbooking.domain.Timeslot;

import java.time.DayOfWeek;

public class WeekendPricingPolicy implements PricingPolicy {
    private final double multiplier;

    public WeekendPricingPolicy(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public double calculatePrice(Resource resource, Timeslot timeslot, double basePrice) {
        DayOfWeek day = timeslot.getStart().getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return basePrice * multiplier;
        }
        return basePrice;
    }

    @Override
    public String getName() {
        return "WEEKEND";
    }
}
