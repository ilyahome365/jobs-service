package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.PropertyTenantExtension;
import com.home365.jobservice.entities.enums.LeaseType;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.service.IPropertyTenantExtensionService;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.utils.DateAndTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeaseUpdatingServiceImpl extends JobExecutorImpl {

    public static final String LEASE_UPDATING_JOB = "Lease Updating Job";
    private final IPropertyTenantExtensionService propertyTenantExtensionService;

    public LeaseUpdatingServiceImpl(AppProperties appProperties,
                                    MailService mailService,
                                    IPropertyTenantExtensionService propertyTenantExtensionService) {
        super(appProperties, mailService);
        this.propertyTenantExtensionService = propertyTenantExtensionService;
    }

    @Override
    protected String getJobName() {
        return LEASE_UPDATING_JOB;
    }

    @Override
    protected String execute() throws Exception {
        Calendar currentCalendar = Calendar.getInstance();

        Calendar nextMonth = Calendar.getInstance();
        nextMonth.add(Calendar.MONTH, 1);

        List<PropertyTenantExtension> leaseToUpdate = propertyTenantExtensionService.getAllActivePlansToUpdate()
                .stream()
                .filter(propertyTenantExtension -> propertyTenantExtension.getLeaseType() != null)
                .peek(propertyTenantExtension -> log.info(String.format("PropertyTenantExtension with id [%s], Lease Type [%s], Days Left [%d]",
                        propertyTenantExtension.getPropertyTenantId(),
                        propertyTenantExtension.getLeaseType().name(),
                        propertyTenantExtension.getDaysLeft())
                ))
                .peek(propertyTenantExtension -> {
                    propertyTenantExtension.setDaysLeft(DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getEndDate()));
                    log.info(String.format("Change PropertyTenantExtension with id [%s], Days Left [%d]",
                            propertyTenantExtension.getPropertyTenantId(),
                            propertyTenantExtension.getDaysLeft())
                    );
                })
                .peek(propertyTenantExtension -> {
                    if (propertyTenantExtension.getLeaseType().equals(LeaseType.Yearly) && propertyTenantExtension.getDaysLeft() <= 0) {
                        propertyTenantExtension.setLeaseType(LeaseType.Monthly);
                        log.info(String.format("Change PropertyTenantExtension with id [%s], to Lease Type [%s]",
                                propertyTenantExtension.getPropertyTenantId(),
                                propertyTenantExtension.getLeaseType().name())
                        );
                    }
                })
                .peek(propertyTenantExtension -> {
                    if (propertyTenantExtension.getLeaseType().equals(LeaseType.Monthly) && propertyTenantExtension.getDaysLeft() <= 0) {
                        propertyTenantExtension.setEndDate(nextMonth.getTime());
                        propertyTenantExtension.setDaysLeft(DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getEndDate()));
                        log.info(String.format("Change PropertyTenantExtension with id [%s], Days Left [%d]",
                                propertyTenantExtension.getPropertyTenantId(),
                                propertyTenantExtension.getDaysLeft())
                        );
                    }
                })
                .collect(Collectors.toList());

        propertyTenantExtensionService.save(leaseToUpdate);
        log.info(getJobName() + " Finished");
        return getJobName() + " Finished ";
    }
}
