package com.smartbooking.domain.policy;

import com.smartbooking.domain.Resource;

public interface ApprovalPolicy {
    boolean requiresApproval(Resource resource);
    String getName();
}
