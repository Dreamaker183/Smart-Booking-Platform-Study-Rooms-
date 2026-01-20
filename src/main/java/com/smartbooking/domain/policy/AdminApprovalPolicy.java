package com.smartbooking.domain.policy;

import com.smartbooking.domain.Resource;

public class AdminApprovalPolicy implements ApprovalPolicy {
    @Override
    public boolean requiresApproval(Resource resource) {
        return true;
    }

    @Override
    public String getName() {
        return "ADMIN_REQUIRED";
    }
}
