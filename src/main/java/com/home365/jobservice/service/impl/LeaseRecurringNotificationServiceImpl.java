package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.IPropertyLeaseInformationProjection;
import com.home365.jobservice.model.LeasePropertyNotificationConfiguration;
import com.home365.jobservice.service.JobsConfigurationService;
import com.home365.jobservice.service.LeaseRecurringNotificationService;
import com.home365.jobservice.service.PropertyService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
@Service
public class LeaseRecurringNotificationServiceImpl implements LeaseRecurringNotificationService {

    private final ReentrantLock lock = new ReentrantLock();
    private final PropertyService propertyService;
    private final JobsConfigurationService jobsConfigurationService;

    public LeaseRecurringNotificationServiceImpl(PropertyService propertyService,
                                                 JobsConfigurationService jobsConfigurationService) {
        this.propertyService = propertyService;
        this.jobsConfigurationService = jobsConfigurationService;
    }

    @Override
    public boolean startLeasePropertyNotification() {
        log.info("Try to Start Lease Property Notification Job");
        if (lock.tryLock()) {
            try {
                log.info("Lease Property Notification Job Started");
                Calendar currentCalendar = Calendar.getInstance();

                LeasePropertyNotificationConfiguration leasePropertyNotificationConfiguration = jobsConfigurationService.getLeasePropertyNotificationConfiguration();

                Calendar futureCalendar = Calendar.getInstance();
                futureCalendar.add(Calendar.DAY_OF_WEEK, leasePropertyNotificationConfiguration.getAmount());

                List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries = getPropertyExtension(currentCalendar, futureCalendar);
                sendMail(leaseExpiryPropertySummaries);
                showSummary(leaseExpiryPropertySummaries);
            } catch (Exception ex) {
                log.info(ex.getMessage());
            } finally {
                lock.unlock();
            }
            log.info("Lease Property Notification Job Finished");
            return true;
        }
        log.info("Lease Property Notification Job didn't Start -> Already Running");
        return false;
    }

    private List<LeaseExpiryPropertySummary> getPropertyExtension(Calendar currentCalendar, Calendar futureCalendar) {
        log.info(String.format("Get Lease Property From Today: [%s] To: [%s]",
                currentCalendar.getTime().toString(),
                futureCalendar.getTime().toString())
        );

        List<IPropertyLeaseInformationProjection> propertyLeaseInformationProjections = propertyService.findAllForLeaseNotification(
                currentCalendar.getTime(),
                futureCalendar.getTime()
        );

        log.info("Create Lease Expiry Property Summary");
        List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");
        propertyLeaseInformationProjections
                .forEach(property -> {
                    try {
                        LeaseExpiryPropertySummary leaseExpiryPropertySummary = new LeaseExpiryPropertySummary();
                        leaseExpiryPropertySummary.setProperty(property.getPropertyName());
                        leaseExpiryPropertySummary.setTenant(property.getTenantName());
                        leaseExpiryPropertySummary.setDaysLeft(getTimeDiff(currentCalendar, property.getEndDate()));
                        leaseExpiryPropertySummary.setExpiredDate(formatter.format(property.getEndDate()));
                        leaseExpiryPropertySummaries.add(leaseExpiryPropertySummary);
                    } catch (Exception ex) {
                        log.warn(ex.getMessage());
                    }
                });
        return leaseExpiryPropertySummaries;
    }

    private long getTimeDiff(Calendar currentCalendar, Date dueDate) {
        Calendar dueDateCalendar = Calendar.getInstance();
        dueDateCalendar.setTime(dueDate);
        long diffInMillies = Math.abs(currentCalendar.getTime().getTime() - dueDate.getTime());
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    private void sendMail(List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries) {

    }

    private void showSummary(List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries) {
        leaseExpiryPropertySummaries.forEach(new Consumer<LeaseExpiryPropertySummary>() {
            @Override
            public void accept(LeaseExpiryPropertySummary leaseExpiryPropertySummary) {
                log.info(leaseExpiryPropertySummary.toString());
            }
        });
    }

    @Data
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaseExpiryPropertySummary {
        private String property;
        private String tenant;
        private long daysLeft;
        private String expiredDate;
    }
}
