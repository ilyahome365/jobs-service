package com.home365.jobservice.rest;


import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.TenantStatusChangeRequest;
import feign.Headers;
import feign.RequestLine;

public interface TenantFeignService {

    @RequestLine("POST /tenant/change-tenant-status")
    @Headers({"Content-Type: application/json"})
    void changeTenantStatus(TenantStatusChangeRequest tenantStatusChangeRequest) throws GeneralException;
}
