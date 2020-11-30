package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.PropertyTenantExtension;
import com.home365.jobservice.entities.enums.LeaseType;
import com.home365.jobservice.entities.projection.ILeaseInformation;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.service.IPropertyTenantExtensionService;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.utils.DateAndTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
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

        // TODO: check moveout

        List<ILeaseInformation> allActiveLeases = propertyTenantExtensionService.getAllActivePlansToUpdate();

        List<String> leaseToUpdateIds = allActiveLeases.stream().map(ILeaseInformation::getPropertyTenantId).collect(Collectors.toList());
        List<PropertyTenantExtension> leaseToUpdate = propertyTenantExtensionService.findAllByIds(leaseToUpdateIds);

        Calendar currentCalendar = Calendar.getInstance();
        leaseToUpdate.forEach(propertyTenantExtension -> {
            if (propertyTenantExtension.getLeaseType().equals(LeaseType.Yearly) && propertyTenantExtension.getDaysLeft() <= 0) {
                propertyTenantExtension.setLeaseType(LeaseType.Monthly);
            }
            Date extendDate = DateAndTimeUtil.addMonths(1, propertyTenantExtension.getEndDate());
            propertyTenantExtension.setEndDate(extendDate);

            int daysLeft = DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getEndDate());
            propertyTenantExtension.setDaysLeft(daysLeft);
        });

        propertyTenantExtensionService.save(leaseToUpdate);
        log.info(getJobName() + " Finished");
        return getJobName() + " Finished ";
    }
}
