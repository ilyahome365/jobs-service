package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.IPropertyLeaseInformationProjection;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.LeasePropertyNotificationConfiguration;
import com.home365.jobservice.model.RecipientMail;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.service.JobsConfigurationService;
import com.home365.jobservice.service.LeasePropertyNotificationService;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.service.PropertyService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
@Service
public class LeaseRecurringNotificationServiceImpl implements LeasePropertyNotificationService {

    private final ReentrantLock lock = new ReentrantLock();
    private final AppProperties appProperties;
    private final PropertyService propertyService;
    private final JobsConfigurationService jobsConfigurationService;
    private final MailService mailService;

    public LeaseRecurringNotificationServiceImpl(AppProperties appProperties,
                                                 PropertyService propertyService,
                                                 JobsConfigurationService jobsConfigurationService,
                                                 MailService mailService) {
        this.appProperties = appProperties;
        this.propertyService = propertyService;
        this.jobsConfigurationService = jobsConfigurationService;
        this.mailService = mailService;
    }

    @Override
    public JobExecutionResults startLeasePropertyNotification() {
        JobExecutionResults jobExecutionResults = new JobExecutionResults();
        log.info("Try to Start Lease Property Notification Job");
        if (lock.tryLock()) {
            try {
                log.info("Lease Property Notification Job Started");
                Calendar currentCalendar = Calendar.getInstance();

                LeasePropertyNotificationConfiguration leasePropertyNotificationConfiguration = jobsConfigurationService.getLeasePropertyNotificationConfiguration();
                List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries = getPropertyExtension(
                        currentCalendar,
                        leasePropertyNotificationConfiguration
                );
                sendMail(leasePropertyNotificationConfiguration, leaseExpiryPropertySummaries);
                showSummary(leaseExpiryPropertySummaries);
                jobExecutionResults.setSucceeded(true);
            } catch (Exception exception) {
                log.info(exception.getMessage());
                jobExecutionResults.setSucceeded(false);
                jobExecutionResults.setError(exception.getMessage());
                jobExecutionResults.setStackTrace(Arrays.toString(exception.getStackTrace()));
            } finally {
                lock.unlock();
            }
            log.info("Lease Property Notification Job Finished");
            return jobExecutionResults;
        }
        log.info("Lease Property Notification Job didn't Start -> Already Running");
        return jobExecutionResults;
    }

    private List<LeaseExpiryPropertySummary> getPropertyExtension(Calendar currentCalendar,
                                                                  LeasePropertyNotificationConfiguration leasePropertyNotificationConfiguration) {
        Calendar futureCalendar = Calendar.getInstance();
        futureCalendar.add(Calendar.DAY_OF_WEEK, leasePropertyNotificationConfiguration.getDays());

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

    private void sendMail(LeasePropertyNotificationConfiguration leasePropertyNotificationConfiguration,
                          List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries) {
        MailDetails mailDetails = new MailDetails();
        mailDetails.setFrom(appProperties.getMailSupport());
        mailDetails.setSubject("Lease expiry");
        mailDetails.setTemplateName(leasePropertyNotificationConfiguration.getEmailTemplateName());
        mailDetails.setContentTemplate(getContentTemplate(leasePropertyNotificationConfiguration, leaseExpiryPropertySummaries));
        mailDetails.setRecipients(Collections.singletonList(new RecipientMail(
                leasePropertyNotificationConfiguration.getToName(),
                leasePropertyNotificationConfiguration.getToMail()
        )));
        mailService.sendMail(mailDetails);
    }

    private Map<String, String> getContentTemplate(LeasePropertyNotificationConfiguration leasePropertyNotificationConfiguration,
                                                   List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries) {
        Map<String, String> contentTemplate = new HashMap<>();
        contentTemplate.put("DAYS", String.valueOf(leasePropertyNotificationConfiguration.getDays()));


        String html = createHTMLTable(leaseExpiryPropertySummaries);


        contentTemplate.put("TABLE", html);
        return contentTemplate;
    }

    private String createHTMLTable(List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<table style=\"width:100%\">")
                .append("<tr>")
                .append("<th>PROPERTY</th>")
                .append("<th>TENANT</th>")
                .append("<th>DAYS</th>")
                .append("<th>EXP</th>")
                .append("<tr>");
        ListUtils.emptyIfNull(leaseExpiryPropertySummaries).forEach(leaseExpiryPropertySummary -> {
            stringBuilder
                    .append("<tr>")
                    .append("<td>").append(leaseExpiryPropertySummary.getProperty()).append("<td>")
                    .append("<td>").append(leaseExpiryPropertySummary.getTenant()).append("<td>")
                    .append("<td>").append(leaseExpiryPropertySummary.getDaysLeft()).append("<td>")
                    .append("<td>").append(leaseExpiryPropertySummary.getExpiredDate()).append("<td>")
                    .append("</tr>");
        });
        stringBuilder.append("</table>");
        return stringBuilder.toString();
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
