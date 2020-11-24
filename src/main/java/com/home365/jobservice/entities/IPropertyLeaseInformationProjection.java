package com.home365.jobservice.entities;

import java.util.Date;

public interface IPropertyLeaseInformationProjection {
    String getPropertyName();

    String getUnit();

    String getBuilding();

    String getTenantName();

    Date getEndDate();
}
