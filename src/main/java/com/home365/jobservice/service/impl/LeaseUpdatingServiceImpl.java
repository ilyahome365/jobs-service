package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LeaseUpdatingServiceImpl extends JobExecutorImpl {

    public static final String LEASE_UPDATING_JOB = "Lease Updating Job";

    public LeaseUpdatingServiceImpl(AppProperties appProperties,
                                    MailService mailService) {
        super(appProperties, mailService);
    }

    @Override
    protected String getJobName() {
        return LEASE_UPDATING_JOB;
    }

    @Override
    protected String execute() throws Exception {

        // Get All Y2Y plans
        //      - Calculate day to finish
        //      - All Y2Y Plans that:
        //              - Ends today
        //              - Moveout is not empty
        //        Change to M2M
        // if Move-out empty
        //      - Add 1 month




        log.info("Lease Property Notification Job Finished");
        return "Lease Property Notification Job Finished";
    }
}
