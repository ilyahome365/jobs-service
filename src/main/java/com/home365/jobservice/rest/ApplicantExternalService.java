package com.home365.jobservice.rest;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.rest.model.ApplicantOccupiedProperty;
import com.home365.jobservice.rest.model.ApplicationRequest;
import com.home365.jobservice.rest.model.PropertyApplicationResponse;

import java.util.List;

public interface ApplicantExternalService {
    void updateApplicantHomeOccupied(ApplicantOccupiedProperty applicantOccupiedProperty);

    List<PropertyApplicationResponse> getPropertyApplicationsByCreationDateAndStatuses(ApplicationRequest applicationRequest) throws GeneralException;
}
