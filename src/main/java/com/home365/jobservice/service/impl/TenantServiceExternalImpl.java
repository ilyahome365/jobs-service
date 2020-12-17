package com.home365.jobservice.service.impl;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.TenantStatusChangeRequest;
import com.home365.jobservice.rest.KeyCloakService;
import com.home365.jobservice.rest.KeycloakResponse;
import com.home365.jobservice.rest.TenantFeignService;
import com.home365.jobservice.rest.TenantServiceExternal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        log.info("Change Tenant Status :  {} ",tenantStatusChangeRequest);
        KeycloakResponse token = keyCloakService.getKey();
        tenantFeignService.changeTenantStatus(token.getAccess_token(),tenantStatusChangeRequest);

    }
}
