package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.config.Constants;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.service.ApplicantsService;
import com.home365.jobservice.service.MailService;
import org.springframework.stereotype.Service;

@Service
public class ApplicantsServiceImpl extends JobExecutorImpl implements ApplicantsService {


    protected ApplicantsServiceImpl(AppProperties appProperties, MailService mailService) {
        super(appProperties, mailService);
    }

    @Override
    public String notifyApplicantsWhoDidntFinishTheFlow() {

        return "";
    }

    @Override
    protected String getJobName() {
        return Constants.REMINDER_APPLICANT_FINISH_THE_FLOW;
    }

    @Override
    protected String execute(String locationId) throws Exception {

        return this.notifyApplicantsWhoDidntFinishTheFlow();
    }
}
