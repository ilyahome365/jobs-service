package com.home365.jobservice.rest;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.TenantStatusChangeRequest;
import com.home365.jobservice.model.wrapper.OwnerWrapper;
import com.home365.jobservice.model.wrapper.TenantWrapper;

import java.util.List;

public interface TenantServiceExternal {
    void changeTenantStatus(TenantStatusChangeRequest tenantStatusChangeRequest) throws GeneralException;
    List<TenantWrapper> getTenantsByPropertyId(String propertyId) throws GeneralException;
    void movePropertyToReadyForDeactivation(String propertyId) throws GeneralException;
    OwnerWrapper getOwnerFromProperty(String propertyId) throws GeneralException;
}
