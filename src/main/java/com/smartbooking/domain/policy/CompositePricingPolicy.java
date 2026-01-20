package com.smartbooking.domain.policy;

import com.smartbooking.domain.Resource;
import com.smartbooking.domain.Timeslot;

import java.util.List;

public class CompositePricingPolicy implements PricingPolicy {
    private final List<PricingPolicy> policies;

    public CompositePricingPolicy(List<PricingPolicy> policies) {
        this.policies = policies;
    }

    @Override
    public double calculatePrice(Resource resource, Timeslot timeslot, double basePrice) {
        double price = basePrice;
        for (PricingPolicy policy : policies) {
            price = policy.calculatePrice(resource, timeslot, price);
        }
        return price;
    }

    @Override
    public String getName() {
        return "COMPOSITE";
    }
}
