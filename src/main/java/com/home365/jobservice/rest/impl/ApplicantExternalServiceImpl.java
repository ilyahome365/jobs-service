package com.home365.jobservice.rest.impl;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.rest.ApplicantExternalService;
import com.home365.jobservice.rest.ApplicantFeignService;
import com.home365.jobservice.rest.KeyCloakService;
import com.home365.jobservice.rest.KeycloakResponse;
import com.home365.jobservice.rest.model.ApplicantOccupiedProperty;
import com.home365.jobservice.rest.model.ApplicationRequest;
import com.home365.jobservice.rest.model.PropertyApplicationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ApplicantExternalServiceImpl implements ApplicantExternalService {
    private final ApplicantFeignService applicantFeignService;
    private final KeyCloakService keyCloakService;

    public ApplicantExternalServiceImpl(ApplicantFeignService applicantFeignService, KeyCloakService keyCloakService) {
        this.applicantFeignService = applicantFeignService;
        this.keyCloakService = keyCloakService;
    }

    @Override
    public void updateApplicantHomeOccupied(ApplicantOccupiedProperty applicantOccupiedProperty) {
        log.info("update applicants for property : {} ", applicantOccupiedProperty.getPropertyId());
        applicantFeignService.updateHomeIsOccupied(applicantOccupiedProperty);
    }

    @Override
    public List<PropertyApplicationResponse> getPropertyApplicationsByCreationDateAndStatuses(ApplicationRequest applicationRequest) throws GeneralException {
        KeycloakResponse key = keyCloakService.getKey();
        return applicantFeignService.getApplicantsByCreationAnd(key.getAccess_token(),applicationRequest);
    }
}
