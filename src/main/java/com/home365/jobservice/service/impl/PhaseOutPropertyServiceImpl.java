package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.PropertyExtension;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.flow.PropertyPhasingOutFlow;
import com.home365.jobservice.model.enums.BusinessType;
import com.home365.jobservice.rest.KeyCloakService;
import com.home365.jobservice.rest.TenantFeignService;
import com.home365.jobservice.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PhaseOutPropertyServiceImpl extends JobExecutorImpl {
    private final String PHASE_OUT_PROPERTY_JOB = "phase-out-property";
    private final TenantFeignService tenantFeignService;
    private final PropertyPhasingOutFlow propertyPhasingOutFlow;
    private final KeyCloakService keyCloakService;

    public PhaseOutPropertyServiceImpl(AppProperties appProperties, MailService mailService, TenantFeignService tenantFeignService, PropertyPhasingOutFlow propertyPhasingOutFlow, KeyCloakService keyCloakService) {
        super(appProperties, mailService);
        this.tenantFeignService = tenantFeignService;

        this.propertyPhasingOutFlow = propertyPhasingOutFlow;
        this.keyCloakService = keyCloakService;
    }

    @Override
    protected String getJobName() {
        return PHASE_OUT_PROPERTY_JOB;
    }

    @Override
    protected String execute(String locationId) throws Exception{
        List<String> propertiesPhaseOut = new ArrayList<>();
        String access_token = keyCloakService.getKey().getAccess_token();
        List<PropertyExtension> propertiesByAccountAndBusinessType = tenantFeignService.getPropertiesByAccountAndBusinessType(access_token, locationId, BusinessType.PM.name());
        for (PropertyExtension propertyExtension : propertiesByAccountAndBusinessType) {
            if (propertyExtension.getPhasingOutDate() != null && (propertyExtension.getPhasingOutDate().isBefore(LocalDate.now()) || propertyExtension.getPhasingOutDate().equals(LocalDate.now()))) {
                try {
                    propertyPhasingOutFlow.startPropertyPhasingOut(propertyExtension.getPropertyId());
                } catch (GeneralException e) {
                   log.error(e.getMessage());
                }
                propertiesPhaseOut.add(propertyExtension.getPropertyId());

            }
        }
        return String.format("%s  run and and properties %s get phase out  ", PHASE_OUT_PROPERTY_JOB, String.join(",", propertiesPhaseOut));
    }
}
