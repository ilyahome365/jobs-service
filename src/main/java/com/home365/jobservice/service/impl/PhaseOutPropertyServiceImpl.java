package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.PropertyExtension;
import com.home365.jobservice.entities.enums.PropertyStatus;
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
import java.util.Objects;
import java.util.stream.Collectors;

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
    protected String execute(String locationId) throws Exception {
        List<String> propertiesPhaseOut = new ArrayList<>();
        String access_token = keyCloakService.getKey().getAccess_token();
        List<PropertyExtension> propertiesByAccountAndBusinessType = tenantFeignService
                .getPropertiesByAccountAndBusinessType(access_token, locationId, BusinessType.PM.name()).stream()
                .filter(Objects::nonNull)
                .filter(propertyExtension -> propertyExtension.getPhasingOutDate() != null &&
                        propertyExtension.getPropertyStatus().equalsIgnoreCase(PropertyStatus.phasingOut.name())
                        && (propertyExtension.getPhasingOutDate().isBefore(LocalDate.now())
                        || propertyExtension.getPhasingOutDate().equals(LocalDate.now()))).collect(Collectors.toList());
        for (PropertyExtension propertyExtension : propertiesByAccountAndBusinessType) {

                try {
                    propertyPhasingOutFlow.startPropertyPhasingOut(propertyExtension.getPropertyId());
                    propertiesPhaseOut.add(propertyExtension.getPropertyId());
                } catch (GeneralException e) {
                    log.error(e.getMessage());
                }


        }
        return String.format("%s  run and and properties %s get phase out  ", PHASE_OUT_PROPERTY_JOB, String.join(",", propertiesPhaseOut));
    }
}
