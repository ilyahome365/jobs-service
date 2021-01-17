package com.home365.jobservice.rest;


import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.TenantStatusChangeRequest;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface TenantFeignService {

    @RequestLine("POST /tenant/change-tenant-status")
    @Headers({"Authorization: Bearer {token}","Content-Type: application/json"})
    void changeTenantStatus(@Param("token") String token, TenantStatusChangeRequest tenantStatusChangeRequest) throws GeneralException;
}
