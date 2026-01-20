package com.smartbooking.domain.policy;

import com.smartbooking.domain.Resource;
import com.smartbooking.domain.Timeslot;

public class DefaultPricingPolicy implements PricingPolicy {
    @Override
    public double calculatePrice(Resource resource, Timeslot timeslot, double basePrice) {
        return basePrice;
    }

    @Override
    public String getName() {
        return "DEFAULT";
    }
}
