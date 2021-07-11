package com.home365.jobservice.rest;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.wrapper.TenantWrapper;
import com.home365.jobservice.rest.model.ApplicantOccupiedProperty;
import com.home365.jobservice.rest.model.ApplicationRequest;
import com.home365.jobservice.rest.model.PropertyApplicationResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface ApplicantFeignService {
    @RequestLine("POST /applicant/applications-by-creation?propertyId={propertyId}")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
   List<PropertyApplicationResponse> getApplicantsByCreationAnd(@Param("token") String token, ApplicationRequest applicationRequest) throws GeneralException;

    @Headers({"Content-Type: application/json"})
    @RequestLine("POST /applicant/update-applicant-home-is-occupied")
    void updateHomeIsOccupied(ApplicantOccupiedProperty applicantOccupiedProperty);
}
