package com.home365.jobservice.rest;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.TenantStatusChangeRequest;

public interface TenantServiceExternal {
    void changeTenantStatus(TenantStatusChangeRequest tenantStatusChangeRequest) throws GeneralException;
}
