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
public class ActivateOwnerServiceImpl extends JobExecutorImpl {


    private final TenantServiceExternal tenantServiceExternal;
    private final ObjectMapper objectMapper;
    public ActivateOwnerServiceImpl(AppProperties appProperties, MailService mailService, TenantServiceExternal tenantServiceExternal, ObjectMapper objectMapper) {
        super(appProperties, mailService);
        this.tenantServiceExternal = tenantServiceExternal;
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getJobName() {
        return Constants.ACTIVE_OWNER;
    }

    @Override
    protected String execute(String locationId) throws Exception {
        log.info("Activating owners");
        List<String> activatedOwners = this.tenantServiceExternal.activatedOwners();
        return objectMapper.writeValueAsString(activatedOwners);
    }
}
