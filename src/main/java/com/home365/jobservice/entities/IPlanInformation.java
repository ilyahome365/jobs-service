package com.home365.jobservice.entities;

import java.util.Date;

public interface IPlanInformation {
    String getPropertyName();

    String getUnit();

    String getBuilding();

    String getTenantName();

    Date getEndDate();
}
