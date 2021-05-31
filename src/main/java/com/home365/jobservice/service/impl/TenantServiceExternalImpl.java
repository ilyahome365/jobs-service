package com.home365.jobservice.service.impl;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.TenantStatusChangeRequest;
import com.home365.jobservice.model.TenantsRequest;
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

    @Override
    public TenantsRequest getTenantByContact(String contactId) throws GeneralException {
        log.info("get tenant by contact id  : {} ", contactId);
        KeycloakResponse token = keyCloakService.getKey();
        return tenantFeignService.getTenantByContactId(token.getAccess_token(), contactId);
    }

    @Override
    public void updateLeasePerTenant(TenantsRequest tenantsRequest) throws GeneralException {
        log.info("update lease for  {} ", tenantsRequest.toString());
        KeycloakResponse token = keyCloakService.getKey();
        tenantFeignService.updateLease(token.getAccess_token(), tenantsRequest);
    }

    @Override
    public List<String> activatedOwners() throws GeneralException {
        log.info("Run request for activation users");
        KeycloakResponse token = keyCloakService.getKey();
        List<String> activatedOwners = tenantFeignService.activateOwners(token.getAccess_token(), " due to  activation of owner");
        log.info("Activated owners : {}",activatedOwners);
        return activatedOwners;
    }

    @Override
    public List<String> sendReminderOfFirstContribution() throws GeneralException {
        log.info("Run request for reminder");
        KeycloakResponse token = keyCloakService.getKey();
        List<String> ownerIds = tenantFeignService.firstContributionReminder(token.getAccess_token());
        return ownerIds;
    }
}
