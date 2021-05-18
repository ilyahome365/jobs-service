package com.home365.jobservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.config.Constants;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.rest.BalanceServiceFeign;
import com.home365.jobservice.rest.KeyCloakService;
import com.home365.jobservice.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CreateWelcomeCreditServiceImpl extends JobExecutorImpl {


    private final BalanceServiceFeign balanceServiceFeign;
    private final KeyCloakService keyCloakService;
    private final ObjectMapper objectMapper;
    public CreateWelcomeCreditServiceImpl(AppProperties appProperties, MailService mailService, BalanceServiceFeign balanceServiceFeign, KeyCloakService keyCloakService, ObjectMapper objectMapper) {
        super(appProperties, mailService);
        this.balanceServiceFeign = balanceServiceFeign;
        this.keyCloakService = keyCloakService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getJobName() {
        return Constants.WELCOME_CREDIT;
    }

    @Override
    protected String execute(String locationId) throws Exception {
        log.info("Creating welcome credit");
        List<String> welcomeCredit = balanceServiceFeign.createWelcomeCredit(keyCloakService.getKey().getAccess_token());
        String planResult = "";
        if(!CollectionUtils.isEmpty(welcomeCredit)){
            planResult = objectMapper.writeValueAsString(welcomeCredit);
        }
        return planResult;
    }
}
