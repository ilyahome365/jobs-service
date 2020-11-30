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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
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
        List<ILeaseInformation> allActiveLeases = propertyTenantExtensionService.getAllActivePlans();

        List<String> leaseToUpdateIds = new ArrayList<>();
        List<String> y2yLeaseToExtendIds = new ArrayList<>();

        Calendar currentCalendar = Calendar.getInstance();
        allActiveLeases.forEach(leaseInformation -> {
            if (leaseInformation.getLeaseType().equals(LeaseType.Monthly) ||
                    leaseInformation.getLeaseType().equals(LeaseType.Yearly) && DateAndTimeUtil.getDaysLeft(currentCalendar, leaseInformation.getEndDate()) > 0
            ) {
                leaseToUpdateIds.add(leaseInformation.getPropertyTenantId());
                return;
            }
            y2yLeaseToExtendIds.add(leaseInformation.getPropertyTenantId());
        });

        List<PropertyTenantExtension> leaseToUpdate = propertyTenantExtensionService.findAllByIds(leaseToUpdateIds);
        List<PropertyTenantExtension> y2yLeaseToExtend = propertyTenantExtensionService.findAllByIds(y2yLeaseToExtendIds);

        leaseToUpdate.forEach(propertyTenantExtension -> {
            int daysLeft = DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getEndDate());
            propertyTenantExtension.setDaysLeft(daysLeft);
        });
        // Then:
        //      - Change to M2M
        //      - Add 1 month
        y2yLeaseToExtend.forEach(propertyTenantExtension -> {
            propertyTenantExtension.setLeaseType(LeaseType.Monthly);

            Date extendDate = DateAndTimeUtil.addMonths(1, propertyTenantExtension.getEndDate());
            propertyTenantExtension.setEndDate(extendDate);

            int daysLeft = DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getEndDate());
            propertyTenantExtension.setDaysLeft(daysLeft);
        });

        propertyTenantExtensionService.save(leaseToUpdate);
        propertyTenantExtensionService.save(y2yLeaseToExtend);
        log.info(getJobName() + " Finished");
        return getJobName() + " Finished ";
    }
}
