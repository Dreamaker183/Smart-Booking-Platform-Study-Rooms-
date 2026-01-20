package com.smartbooking.domain.policy;

import com.smartbooking.domain.Resource;

public class AutoApprovalPolicy implements ApprovalPolicy {
    @Override
    public boolean requiresApproval(Resource resource) {
        return false;
    }

    @Override
    public String getName() {
        return "AUTO";
    }
}
