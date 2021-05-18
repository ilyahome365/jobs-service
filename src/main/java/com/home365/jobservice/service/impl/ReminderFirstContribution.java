package com.home365.jobservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.config.Constants;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.rest.TenantServiceExternal;
import com.home365.jobservice.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ReminderFirstContribution  extends JobExecutorImpl {


    private final TenantServiceExternal tenantServiceExternal;
    private final ObjectMapper objectMapper;
    public ReminderFirstContribution(AppProperties appProperties, MailService mailService, TenantServiceExternal tenantServiceExternal, ObjectMapper objectMapper) {
        super(appProperties, mailService);
        this.tenantServiceExternal = tenantServiceExternal;
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getJobName() {
        return Constants.REMINDER_FIRST_CONTRIBUTION;
    }

    @Override
    protected String execute(String locationId) throws Exception {
        log.info("Running reminder for owners");
        List<String> activatedOwners = tenantServiceExternal.activatedOwners();
        return objectMapper.writeValueAsString(activatedOwners);
    }
}
