package com.home365.jobservice.entities.projection;

import com.home365.jobservice.entities.enums.LeaseType;

import java.util.Date;

public interface ILeaseInformation {

    String getTenantName();

    Date getEndDate();

    LeaseType getLeaseType();

    String getPropertyTenantId();
}
