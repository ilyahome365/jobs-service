package com.home365.jobservice.model.enums;

public enum BusinessType {
    Administrator(1),
    HomeWarranty(2),
    Vendor(3),
    PM(6),
    ServiceProvider(7),
    RealEstateOwner(8),
    ContactPerson(9),
    Tenant(10),
    ExternalAccount(21);

    public final int value;


    BusinessType(int label) {
        this.value = label;
    }

    public int getValue() {
        return value;
    }
}
