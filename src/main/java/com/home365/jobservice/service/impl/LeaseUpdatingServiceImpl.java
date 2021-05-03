package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.AccountExtensionBase;
import com.home365.jobservice.entities.PropertyTenantExtension;
import com.home365.jobservice.entities.enums.LeaseType;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.model.TenantStatusChangeRequest;
import com.home365.jobservice.model.TenantsRequest;
import com.home365.jobservice.repository.AccountExtensionRepo;
import com.home365.jobservice.rest.TenantServiceExternal;
import com.home365.jobservice.service.IPropertyTenantExtensionService;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.utils.CurrencyConverter;
import com.home365.jobservice.utils.DateAndTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LeaseUpdatingServiceImpl extends JobExecutorImpl {

    public static final String LEASE_UPDATING_JOB = "Lease Updating Job";
    private final IPropertyTenantExtensionService propertyTenantExtensionService;
    private final TenantServiceExternal tenantServiceExternal;


    private final AccountExtensionRepo accountExtensionRepo;

    public LeaseUpdatingServiceImpl(AppProperties appProperties,
                                    MailService mailService,
                                    IPropertyTenantExtensionService propertyTenantExtensionService, TenantServiceExternal tenantServiceExternal, AccountExtensionRepo accountExtensionRepo) {
        super(appProperties, mailService);
        this.propertyTenantExtensionService = propertyTenantExtensionService;
        this.tenantServiceExternal = tenantServiceExternal;
        this.accountExtensionRepo = accountExtensionRepo;
    }

    @Override
    protected String getJobName() {
        return LEASE_UPDATING_JOB;
    }

    @Override
    public String execute(String locationId) throws Exception {
        LocalDate currentCalendar = LocalDate.now();
        LocalDate nextMonth = LocalDate.now().plusMonths(1);

        List<PropertyTenantExtension> leaseToUpdate = new ArrayList<>();
        for (PropertyTenantExtension propertyTenantExtension : propertyTenantExtensionService.getAllActivePlansToUpdate(locationId)) {
            if (propertyTenantExtension.getLeaseType() != null && propertyTenantExtension.getEndDate() != null) {
                propertyTenantExtension.setDaysLeft(DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getEndDate().toLocalDate()));
                log.info(String.format("PropertyTenantExtension with id [%s], Lease Type [%s], Days Left [%d]",
                        propertyTenantExtension.getPropertyTenantId(),
                        propertyTenantExtension.getLeaseType().name(),
                        propertyTenantExtension.getDaysLeft()));
                if (propertyTenantExtension.getDaysLeft() <= 0) {
                    if (propertyTenantExtension.getMoveOutDate() != null) {
                        int moveOutLeft = DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getMoveOutDate());
                        if (moveOutLeft <= 0) {
                            changePropertyTenantToInactive(propertyTenantExtension);

                        } else {
                            changePropertyTenantLease(propertyTenantExtension, LeaseType.Monthly, propertyTenantExtension.getMoveOutDate().atStartOfDay());
                        }

                    } else {
                        changePropertyTenantLease(propertyTenantExtension, LeaseType.Monthly, nextMonth.atStartOfDay());
                    }
                }
                leaseToUpdate.add(propertyTenantExtension);
            }
        }


        propertyTenantExtensionService.save(leaseToUpdate);
        log.info(getJobName() + " Finished");
        return getJobName() + " Finished ";
    }

    private void changePropertyTenantLease(PropertyTenantExtension propertyTenantExtension, LeaseType leaseType, LocalDateTime endDate) throws GeneralException {
        LocalDate currentCalendar = LocalDate.now();
        propertyTenantExtension.setLeaseType(leaseType);
        propertyTenantExtension.setEndDate(endDate);
        propertyTenantExtension.setDaysLeft(DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getEndDate().toLocalDate()));
        log.info(String.format("Change PropertyTenantExtension with id [%s], to Lease Type [%s] and days left [%d]",
                propertyTenantExtension.getPropertyTenantId(),
                propertyTenantExtension.getLeaseType().name(),
                propertyTenantExtension.getDaysLeft()));
        TenantsRequest tenantByContact = tenantServiceExternal.getTenantByContact(propertyTenantExtension.getContactId());
        tenantByContact.getLeaseDetails().setType(leaseType);
        tenantByContact.getLeaseDetails().setEndDate(endDate);
        if (tenantByContact.getLeaseDetails().getTotalRent()!= null )
            tenantByContact.getLeaseDetails().setTotalRent(CurrencyConverter.toDollar(tenantByContact.getLeaseDetails().getTotalRent()).longValue());
        tenantServiceExternal.updateLeasePerTenant(tenantByContact);
    }

    private void changePropertyTenantToInactive(PropertyTenantExtension propertyTenantExtension) {
        propertyTenantExtension.setActive(false);
        propertyTenantExtension.setInactiveDate(LocalDate.now());
        TenantStatusChangeRequest tenantStatusChangeRequest = new TenantStatusChangeRequest();
        tenantStatusChangeRequest.setContactId(propertyTenantExtension.getContactId());
        tenantStatusChangeRequest.setCreateToPhaseOut(true);
        tenantStatusChangeRequest.setBusinessType(10);
        List<AccountExtensionBase> accounts = accountExtensionRepo.findAccountsByContactIdAndBusinessType(propertyTenantExtension.getContactId(), 10);
        if (!CollectionUtils.isEmpty(accounts))
            tenantStatusChangeRequest.setAccountId(accounts.get(0).getAccountId());
        try {
            tenantServiceExternal.changeTenantStatus(tenantStatusChangeRequest);
        } catch (GeneralException e) {
            e.printStackTrace();
            log.error(e.getMessage());

        }
    }
}
