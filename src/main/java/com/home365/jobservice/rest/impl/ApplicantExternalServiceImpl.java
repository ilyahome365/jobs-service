package com.home365.jobservice.rest.impl;

import com.home365.jobservice.rest.ApplicantExternalService;
import com.home365.jobservice.rest.ApplicantFeignService;
import com.home365.jobservice.rest.model.ApplicantOccupiedProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApplicantExternalServiceImpl implements ApplicantExternalService {
    private final ApplicantFeignService applicantFeignService;

    public ApplicantExternalServiceImpl(ApplicantFeignService applicantFeignService) {
        this.applicantFeignService = applicantFeignService;
    }

    @Override
    public void updateApplicantHomeOccupied(ApplicantOccupiedProperty applicantOccupiedProperty) {
        log.info("update applicants for property : {} ", applicantOccupiedProperty.getPropertyId());
        applicantFeignService.updateHomeIsOccupied(applicantOccupiedProperty);
    }
}
