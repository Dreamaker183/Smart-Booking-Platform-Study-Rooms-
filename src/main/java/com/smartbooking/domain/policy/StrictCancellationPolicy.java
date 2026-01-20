package com.smartbooking.domain.policy;

public class StrictCancellationPolicy implements CancellationPolicy {
    @Override
    public double refundPercent(long hoursBeforeStart) {
        if (hoursBeforeStart >= 72) {
            return 0.8;
        }
        return 0.0;
    }

    @Override
    public String getName() {
        return "STRICT";
    }
}
