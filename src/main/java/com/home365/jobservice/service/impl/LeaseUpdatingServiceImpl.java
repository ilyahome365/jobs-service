package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.PropertyAccountExtension;
import com.home365.jobservice.entities.PropertyTenantExtension;
import com.home365.jobservice.entities.enums.LeaseType;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.model.TenantStatusChangeRequest;
import com.home365.jobservice.repository.PropertyAccountRepository;
import com.home365.jobservice.rest.TenantFeignService;
import com.home365.jobservice.service.IPropertyTenantExtensionService;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.utils.DateAndTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeaseUpdatingServiceImpl extends JobExecutorImpl {

    public static final String LEASE_UPDATING_JOB = "Lease Updating Job";
    private final IPropertyTenantExtensionService propertyTenantExtensionService;
    private final TenantFeignService tenantFeignService;
    private LocalDate currentCalendar = LocalDate.now();
    private PropertyAccountRepository propertyAccountRepository;

    public LeaseUpdatingServiceImpl(AppProperties appProperties,
                                    MailService mailService,
                                    IPropertyTenantExtensionService propertyTenantExtensionService, TenantFeignService tenantFeignService, PropertyAccountRepository propertyAccountRepository) {
        super(appProperties, mailService);
        this.propertyTenantExtensionService = propertyTenantExtensionService;
        this.tenantFeignService = tenantFeignService;
        this.propertyAccountRepository = propertyAccountRepository;
    }

    @Override
    protected String getJobName() {
        return LEASE_UPDATING_JOB;
    }

    @Override
    protected String execute() throws Exception {

        LocalDate nextMonth = LocalDate.now().plusMonths(1);

        List<PropertyTenantExtension> leaseToUpdate = propertyTenantExtensionService.getAllActivePlansToUpdate().stream().
                filter(propertyTenantExtension -> propertyTenantExtension.getLeaseType() != null)
                .peek(propertyTenantExtension -> {
                            propertyTenantExtension.setDaysLeft(DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getEndDate().toLocalDate()));
                            log.info(String.format("PropertyTenantExtension with id [%s], Lease Type [%s], Days Left [%d]",
                                    propertyTenantExtension.getPropertyTenantId(),
                                    propertyTenantExtension.getLeaseType().name(),
                                    propertyTenantExtension.getDaysLeft()));
                        }
                )
                .peek(propertyTenantExtension -> {
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
                }).collect(Collectors.toList());


        propertyTenantExtensionService.save(leaseToUpdate);
        log.info(getJobName() + " Finished");
        return getJobName() + " Finished ";
    }

    private void changePropertyTenantLease(PropertyTenantExtension propertyTenantExtension, LeaseType leaseType, LocalDateTime endDate) {
        propertyTenantExtension.setLeaseType(leaseType);
        propertyTenantExtension.setEndDate(endDate);
        propertyTenantExtension.setDaysLeft(DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getEndDate().toLocalDate()));
        log.info(String.format("Change PropertyTenantExtension with id [%s], to Lease Type [%s] and days left [%d]",
                propertyTenantExtension.getPropertyTenantId(),
                propertyTenantExtension.getLeaseType().name(),
                propertyTenantExtension.getDaysLeft()));
    }

    private void changePropertyTenantToInactive(PropertyTenantExtension propertyTenantExtension) {
        propertyTenantExtension.setActive(false);
        propertyTenantExtension.setInactiveDate(LocalDate.now());
        TenantStatusChangeRequest tenantStatusChangeRequest = new TenantStatusChangeRequest();
        tenantStatusChangeRequest.setContactId(propertyTenantExtension.getContactId());
        tenantStatusChangeRequest.setCreateToPhaseOut(true);
        Optional<PropertyAccountExtension> propertyAccount = propertyAccountRepository.findById(propertyTenantExtension.getPropertAccountId());
        propertyAccount.ifPresent(propertyAccountExtension -> tenantStatusChangeRequest.setAccountId(propertyAccountExtension.getAccountId()));
        try {
            tenantFeignService.changeTenantStatus(tenantStatusChangeRequest);
        } catch (GeneralException e) {
            e.printStackTrace();
            log.error(e.getMessage());

        }
    }
}