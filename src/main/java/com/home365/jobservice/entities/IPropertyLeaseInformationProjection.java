package com.home365.jobservice.entities;

import java.util.Date;

public interface IPropertyLeaseInformationProjection {
    String getPropertyName();

    String getTenantName();

    Date getEndDate();
}
