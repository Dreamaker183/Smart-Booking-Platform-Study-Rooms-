package com.smartbooking.domain;

public class Resource {
    private final long id;
    private final String name;
    private final ResourceType type;
    private final double basePricePerHour;
    private final String pricingPolicyKey;
    private final String cancellationPolicyKey;
    private final String approvalPolicyKey;

    public Resource(long id, String name, ResourceType type, double basePricePerHour,
                    String pricingPolicyKey, String cancellationPolicyKey, String approvalPolicyKey) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.basePricePerHour = basePricePerHour;
        this.pricingPolicyKey = pricingPolicyKey;
        this.cancellationPolicyKey = cancellationPolicyKey;
        this.approvalPolicyKey = approvalPolicyKey;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ResourceType getType() {
        return type;
    }

    public double getBasePricePerHour() {
        return basePricePerHour;
    }

    public String getPricingPolicyKey() {
        return pricingPolicyKey;
    }

    public String getCancellationPolicyKey() {
        return cancellationPolicyKey;
    }

    public String getApprovalPolicyKey() {
        return approvalPolicyKey;
    }
}
