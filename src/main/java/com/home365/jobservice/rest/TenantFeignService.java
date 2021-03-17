package com.home365.jobservice.rest;


import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.TenantStatusChangeRequest;
import com.home365.jobservice.model.wrapper.TenantWrapper;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface TenantFeignService {

    @RequestLine("POST /tenant/change-tenant-status")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    void changeTenantStatus(@Param("token") String token, TenantStatusChangeRequest tenantStatusChangeRequest) throws GeneralException;

    @RequestLine("GET /tenant/get-tenant-by-property?propertyId={propertyId}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    List<TenantWrapper> getTenantByProperty(@Param("token") String token, @Param("propertyId") String propertyId) throws GeneralException;
}
