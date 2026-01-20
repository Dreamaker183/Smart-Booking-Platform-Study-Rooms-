package com.smartbooking.domain.policy;

public class FlexibleCancellationPolicy implements CancellationPolicy {
    @Override
    public double refundPercent(long hoursBeforeStart) {
        if (hoursBeforeStart >= 24) {
            return 1.0;
        }
        if (hoursBeforeStart >= 2) {
            return 0.5;
        }
        return 0.0;
    }

    @Override
    public String getName() {
        return "FLEXIBLE";
    }
}
