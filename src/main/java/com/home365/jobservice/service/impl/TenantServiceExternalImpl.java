package com.home365.jobservice.service.impl;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.TenantStatusChangeRequest;
import com.home365.jobservice.model.wrapper.OwnerWrapper;
import com.home365.jobservice.model.wrapper.TenantWrapper;
import com.home365.jobservice.rest.KeyCloakService;
import com.home365.jobservice.rest.KeycloakResponse;
import com.home365.jobservice.rest.TenantFeignService;
import com.home365.jobservice.rest.TenantServiceExternal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TenantServiceExternalImpl implements TenantServiceExternal {
    private final KeyCloakService keyCloakService;
    private final TenantFeignService tenantFeignService;

    public TenantServiceExternalImpl(KeyCloakService keyCloakService, TenantFeignService tenantFeignService) {
        this.keyCloakService = keyCloakService;
        this.tenantFeignService = tenantFeignService;
    }

    @Override
    public void changeTenantStatus(TenantStatusChangeRequest tenantStatusChangeRequest) throws GeneralException {
        log.info("Change Tenant Status :  {} ", tenantStatusChangeRequest);
        KeycloakResponse token = keyCloakService.getKey();
        tenantFeignService.changeTenantStatus(token.getAccess_token(), tenantStatusChangeRequest);

    }

    @Override
    public List<TenantWrapper> getTenantsByPropertyId(String propertyId) throws GeneralException {
        KeycloakResponse token = keyCloakService.getKey();

        return tenantFeignService.getTenantByProperty(token.getAccess_token(), propertyId);
    }

    @Override
    public void movePropertyToReadyForDeactivation(String propertyId) throws GeneralException {
        log.info("Start property ready for deactivation property : {} ", propertyId);
        KeycloakResponse token = keyCloakService.getKey();
        tenantFeignService.movePropertyToReadyForDeactivation(token.getAccess_token(), propertyId);
    }

    @Override
    public OwnerWrapper getOwnerFromProperty(String propertyId) throws GeneralException {
        KeycloakResponse token = keyCloakService.getKey();
        return tenantFeignService.getOwnerAccountFromProperty(token.getAccess_token(), propertyId);
    }
}
