package com.smartbooking.domain.policy;

public interface CancellationPolicy {
    double refundPercent(long hoursBeforeStart);
    String getName();
}
