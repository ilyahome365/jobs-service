package com.home365.jobservice.rest;


import com.home365.jobservice.entities.PropertyExtension;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.TenantStatusChangeRequest;
import com.home365.jobservice.model.wrapper.OwnerWrapper;
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

    @RequestLine("GET /property/move-property-to-ready-for-deactivation?propertyId={propertyId}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    void movePropertyToReadyForDeactivation(@Param("token") String token, @Param("propertyId") String propertyId) throws GeneralException;

    @RequestLine("GET /tenant/get-owner-account-from-property?propertyId={propertyId}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    OwnerWrapper getOwnerAccountFromProperty(@Param("token") String token, @Param("propertyId") String propertyId) throws GeneralException;

    @RequestLine("GET /property/get-properties-by-account-and-business-type?accountId={accountId}&businessType={businessType}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    List<PropertyExtension> getPropertiesByAccountAndBusinessType(@Param("token") String token, @Param("accountId") String accountId, @Param("businessType") String businessType);


}
