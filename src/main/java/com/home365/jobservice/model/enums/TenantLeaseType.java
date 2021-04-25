package com.home365.jobservice.model.enums;

public enum TenantLeaseType {
    Monthly("monthly"),
    Yearly("endOfTerm");

    private final String value;

    TenantLeaseType(String value) {
        this.value = value;
    }

    public static TenantLeaseType findByName(String type) {
        for(TenantLeaseType value : values()){
            if(value.value.equals(type)){
                return value;
            }
        }
        return Monthly;
    }
}
