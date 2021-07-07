package com.home365.jobservice.rest;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.wrapper.TenantWrapper;
import com.home365.jobservice.rest.model.ApplicantOccupiedProperty;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface ApplicantFeignService {
    @RequestLine("GET /applicant/applications-by-creation?propertyId={propertyId}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    List<TenantWrapper> getTenantByProperty(@Param("token") String token, @Param("propertyId") String propertyId) throws GeneralException;

    @Headers({"Content-Type: application/json"})
    @RequestLine("POST /applicant/update-applicant-home-is-occupied")
    void updateHomeIsOccupied(ApplicantOccupiedProperty applicantOccupiedProperty);
}
