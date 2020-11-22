package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.JobLog;
import com.home365.jobservice.entities.RecurrentPropertyTenantProjection;
import com.home365.jobservice.entities.Recurring;
import com.home365.jobservice.service.JobLogService;
import com.home365.jobservice.service.LeaseRecurringNotificationService;
import com.home365.jobservice.service.RecurringService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeaseRecurringNotificationServiceImpl implements LeaseRecurringNotificationService {

    private final ReentrantLock lock = new ReentrantLock();
    private final JobLogService jobLogService;
    private final RecurringService recurringService;

    public LeaseRecurringNotificationServiceImpl(JobLogService jobLogService,
                                                 RecurringService recurringService) {
        this.jobLogService = jobLogService;
        this.recurringService = recurringService;
    }

    @Override
    public boolean startLeaseRecurringNotification() {
        log.info("Try to Start Lease Recurring Notification Job");
        JobLog jobLog = new JobLog();
        jobLog.setDate(new Timestamp(new Date().getTime()));
        jobLog.setJobName("Lease Recurring Notification Job");
        if (lock.tryLock()) {
            try {
                log.info("Lease Recurring Notification Job Started");
                Calendar currentCalendar = Calendar.getInstance();

                Calendar futureCalendar = Calendar.getInstance();
                futureCalendar.add(Calendar.DAY_OF_WEEK, 60);

                List<Recurring> leaseRecurring = getRecurring(currentCalendar, futureCalendar);
                List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries = createLeaseExpiryPropertySummary(currentCalendar, leaseRecurring);
                sendMail(leaseExpiryPropertySummaries);
                showSummary(leaseExpiryPropertySummaries, jobLog);
            } catch (Exception ex) {
                log.info(ex.getMessage());
                jobLog.setComments("Exception: " + ex.getMessage());
            } finally {
                lock.unlock();
            }
            log.info("Lease Recurring Notification Job Finished");
            jobLog.setStatus("Finished");
            return true;
        }
        log.info("Lease Recurring Notification Job didn't Start -> Already Running");
        jobLog.setStatus("didn't Start -> Already Running");
        jobLogService.saveJobLog(jobLog);
        return false;
    }

    private List<Recurring> getRecurring(Calendar currentCalendar, Calendar futureCalendar) {
        log.info(String.format("Get Lease Recurring From Today: [%s] To: [%s]",
                currentCalendar.getTime().toString(),
                futureCalendar.getTime().toString())
        );
        return recurringService.findAllForLeaseNotification(currentCalendar.getTime(), futureCalendar.getTime());
    }

    private List<LeaseExpiryPropertySummary> createLeaseExpiryPropertySummary(Calendar currentCalendar,
                                                                              List<Recurring> leaseRecurring) {
        log.info("Create Lease Expiry Property Summary");
        List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries = new ArrayList<>();
        Map<String, RecurrentPropertyTenantProjection> recurrentPropertyAndTenant = getRecurrentPropertyAndTenant(leaseRecurring);
        leaseRecurring.stream()
                .filter(recurring -> recurrentPropertyAndTenant.containsKey(recurring.getId()))
                .forEach(recurring -> {
                    RecurrentPropertyTenantProjection found = recurrentPropertyAndTenant.get(recurring.getId());
                    if (found != null) {
                        LeaseExpiryPropertySummary leaseExpiryPropertySummary = new LeaseExpiryPropertySummary();
                        leaseExpiryPropertySummary.setProperty(found.getPropertyName());
                        leaseExpiryPropertySummary.setTenant(found.getTenantName());
                        leaseExpiryPropertySummary.setDaysLeft(getTimeDiff(currentCalendar, recurring.getDueDate()));
                        leaseExpiryPropertySummary.setExpiredDate(recurring.getDueDate());
                        leaseExpiryPropertySummaries.add(leaseExpiryPropertySummary);
                    }
                });
        return leaseExpiryPropertySummaries;
    }

    private Map<String, RecurrentPropertyTenantProjection> getRecurrentPropertyAndTenant(List<Recurring> leaseRecurring) {
        List<String> recurringIds = leaseRecurring.stream().map(Recurring::getId).collect(Collectors.toList());
        List<RecurrentPropertyTenantProjection> recurrentPropertyAndTenant = recurringService.getRecurrentPropertyAndTenantByRecurringIds(recurringIds);
        return recurrentPropertyAndTenant
                .stream()
                .collect(Collectors.toMap(
                        RecurrentPropertyTenantProjection::getRecurrentId,
                        recurrentPropertyTenantProjection -> recurrentPropertyTenantProjection)
                );
    }

    private int getTimeDiff(Calendar currentCalendar, Date dueDate) {
        return 0;
    }

    private void sendMail(List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries) {

    }

    private void showSummary(List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries, JobLog jobLog) {
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
        private int daysLeft;
        private Date expiredDate;
    }
}
