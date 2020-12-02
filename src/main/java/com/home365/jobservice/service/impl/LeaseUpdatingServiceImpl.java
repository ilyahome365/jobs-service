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
import java.util.Date;
import java.util.List;

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

        // Check if this is the last day of the month
        Calendar currentCalendar = Calendar.getInstance();
        int lastDayOfTheMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDayOfTheMonth = currentCalendar.get(Calendar.DAY_OF_MONTH);
        if (currentDayOfTheMonth != lastDayOfTheMonth) {
            String message = String.format(" Not run -> this is not the last day of the month, Current day [%s] last Day [%s]", currentDayOfTheMonth, lastDayOfTheMonth);
            log.info(getJobName() + message);
            return getJobName() + message;
        }

        // TODO: check moveout

        Calendar nextMonth = Calendar.getInstance();
        nextMonth.add(Calendar.MONTH, 1);
        List<PropertyTenantExtension> leaseToUpdate = propertyTenantExtensionService.getAllActivePlansToUpdate();
        leaseToUpdate.forEach(propertyTenantExtension -> {
            propertyTenantExtension.setDaysLeft(DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getEndDate()));

            if (propertyTenantExtension.getLeaseType() != null && propertyTenantExtension.getLeaseType().equals(LeaseType.Yearly) && propertyTenantExtension.getDaysLeft() <= 0) {
                propertyTenantExtension.setLeaseType(LeaseType.Monthly);
            }

            if(propertyTenantExtension.getLeaseType() != null && propertyTenantExtension.getLeaseType().equals(LeaseType.Monthly) && propertyTenantExtension.getDaysLeft() <= 0){
                propertyTenantExtension.setEndDate(nextMonth.getTime());
                propertyTenantExtension.setDaysLeft(DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getEndDate()));
            }
        });

        propertyTenantExtensionService.save(leaseToUpdate);
        log.info(getJobName() + " Finished");
        return getJobName() + " Finished ";
    }
}
