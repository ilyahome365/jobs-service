package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.IPlanInformation;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.service.PropertyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class LeaseUpdatingServiceImpl extends JobExecutorImpl {

    public static final String LEASE_UPDATING_JOB = "Lease Updating Job";
    private final PropertyService propertyService;

    public LeaseUpdatingServiceImpl(AppProperties appProperties,
                                    MailService mailService,
                                    PropertyService propertyService) {
        super(appProperties, mailService);
        this.propertyService = propertyService;
    }

    @Override
    protected String getJobName() {
        return LEASE_UPDATING_JOB;
    }

    @Override
    protected String execute() throws Exception {
        List<IPlanInformation> plans = propertyService.getAllActivePlans();

        // Get All Plans.
        // Then:
        //      - Calculate day to finish
        // To All Y2Y Plans that:
        //      - Ends today
        //      - Moveout is empty
        // Then:
        //      - Change to M2M
        //      - Add 1 month

        log.info(getJobName() + " Finished");
        return getJobName() + "Finished";
    }
}
