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
        // Get All Lease.
        List<ILeaseInformation> allActiveLeases = propertyTenantExtensionService.getAllActivePlans();

        Calendar currentCalendar = Calendar.getInstance();
        // To All Y2Y Plans that:
        List<String> m2mLeaseToUpdateIds = allActiveLeases
                .stream()
                .filter(leaseInformation -> leaseInformation.getLeaseType().equals(LeaseType.Monthly))
                .map(ILeaseInformation::getPropertyTenantId)
                .collect(Collectors.toList());

        // To All Y2Y Plans that:
        //      - Ends today
        //      - Moveout is empty
        StringBuilder stringBuilder = new StringBuilder();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        List<String> y2yLeaseToUpdateIds = allActiveLeases
                .stream()
                .filter(leaseInformation -> leaseInformation.getLeaseType().equals(LeaseType.Yearly))
                .filter(allInformation -> DateAndTimeUtil.getDaysLeft(currentCalendar, allInformation.getEndDate()) <= 0)
//                .filter(leaseInformationEndsToday -> !leaseInformationEndsToday.isMoveout()) // TODO: Add when moveout added
                .map(leaseInformation -> {
                    String date = null;
                    if (leaseInformation.getEndDate() != null) {
                        date = format.format(leaseInformation.getEndDate());
                    }
                    log.info(String.format("Property Tenant Id [%s] with Type [%s] with End Date [%s] will be update",
                            leaseInformation.getPropertyTenantId(),
                            leaseInformation.getLeaseType().name(),
                            date
                    ));
                    return leaseInformation.getPropertyTenantId();
                })
                .collect(Collectors.toList());

        List<PropertyTenantExtension> m2mLeaseToUpdate = propertyTenantExtensionService.findAllByIds(m2mLeaseToUpdateIds);
        m2mLeaseToUpdate.forEach(propertyTenantExtension -> {
            int daysLeft = DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getEndDate());
            propertyTenantExtension.setDaysLeft(daysLeft);
        });

        // Then:
        //      - Change to M2M
        //      - Add 1 month
        List<PropertyTenantExtension> y2yLeaseToExtend = propertyTenantExtensionService.findAllByIds(y2yLeaseToUpdateIds);
        y2yLeaseToExtend.forEach(propertyTenantExtension -> {
            propertyTenantExtension.setLeaseType(LeaseType.Monthly);

            Date extendDate = DateAndTimeUtil.addMonths(1, propertyTenantExtension.getEndDate());
            propertyTenantExtension.setEndDate(extendDate);

            int daysLeft = DateAndTimeUtil.getDaysLeft(currentCalendar, propertyTenantExtension.getEndDate());
            propertyTenantExtension.setDaysLeft(daysLeft);
        });

        propertyTenantExtensionService.save(m2mLeaseToUpdate);
        propertyTenantExtensionService.save(y2yLeaseToExtend);
        log.info(getJobName() + " Finished");
        return getJobName() + " Finished ";
    }
}
