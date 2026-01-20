package com.smartbooking.domain.policy;

import com.smartbooking.domain.Resource;
import com.smartbooking.domain.Timeslot;

public interface PricingPolicy {
    double calculatePrice(Resource resource, Timeslot timeslot, double basePrice);
    String getName();
}
