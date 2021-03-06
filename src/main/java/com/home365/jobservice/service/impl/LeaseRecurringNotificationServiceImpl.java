package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.config.Constants;
import com.home365.jobservice.entities.projection.IPropertyLeaseInformationProjection;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.model.LeasePropertyNotificationConfiguration;
import com.home365.jobservice.model.RecipientMail;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.model.mail.MailResult;
import com.home365.jobservice.service.JobsConfigurationService;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.service.PropertyService;
import com.home365.jobservice.utils.AddressUtils;
import com.home365.jobservice.utils.DateAndTimeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeaseRecurringNotificationServiceImpl extends JobExecutorImpl {

    public static final String _TH = "</th>";
    public static final String TH = "<th>";
    public static final String _TD = "</td>";
    public static final String TD = "<td>";
    public static final String TR = "<tr>";
    public static final String _TR = "</tr>";

    private final PropertyService propertyService;
    private final JobsConfigurationService jobsConfigurationService;

    public LeaseRecurringNotificationServiceImpl(AppProperties appProperties,
                                                 MailService mailService,
                                                 PropertyService propertyService,
                                                 JobsConfigurationService jobsConfigurationService) {
        super(appProperties, mailService);
        this.propertyService = propertyService;
        this.jobsConfigurationService = jobsConfigurationService;
    }

    @Override
    protected String getJobName() {
        return Constants.LEASE_PROPERTY_NOTIFICATION_JOB;
    }

    @Override
    public String execute(String locationId) throws Exception {
        Calendar currentCalendar = Calendar.getInstance();
        LeasePropertyNotificationConfiguration leasePropertyNotificationConfiguration = jobsConfigurationService.getLeasePropertyNotificationConfiguration();
        List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries = getPropertyExtension(
                currentCalendar,
                leasePropertyNotificationConfiguration
        );
        sendMail(leasePropertyNotificationConfiguration, leaseExpiryPropertySummaries);
        showSummary(leaseExpiryPropertySummaries);
        log.info("Lease Property Notification Job Finished");
        return "Lease Property Notification Job Finished";
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
                        leaseExpiryPropertySummary.setProperty(AddressUtils.addUnitAndBuildingToAddress(property.getPropertyName(), property.getUnit(), property.getBuilding()));
                        leaseExpiryPropertySummary.setTenant(property.getTenantName());
                        leaseExpiryPropertySummary.setDaysLeft(DateAndTimeUtil.getTimeDiff(currentCalendar, property.getEndDate()));
                        leaseExpiryPropertySummary.setExpiredDate(formatter.format(property.getEndDate()));
                        leaseExpiryPropertySummaries.add(leaseExpiryPropertySummary);
                    } catch (Exception ex) {
                        log.warn(ex.getMessage());
                    }
                });
        return leaseExpiryPropertySummaries;
    }

    private void sendMail(LeasePropertyNotificationConfiguration leasePropertyNotificationConfiguration,
                          List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries) {
        MailDetails mailDetails = new MailDetails();
        mailDetails.setFrom(appProperties.getMailSupport());
        mailDetails.setSubject("Lease expiry");
        mailDetails.setTemplateName(leasePropertyNotificationConfiguration.getEmailTemplateName());
        mailDetails.setContentTemplate(getContentTemplate(leasePropertyNotificationConfiguration, leaseExpiryPropertySummaries));

        List<RecipientMail> recipientsMail = ListUtils.emptyIfNull(leasePropertyNotificationConfiguration.getToMail())
                .stream()
                .map(mail -> new RecipientMail(leasePropertyNotificationConfiguration.getToName(), mail))
                .collect(Collectors.toList());

        mailDetails.setRecipients(recipientsMail);
        MailResult mailResult = mailService.sendMail(mailDetails);
        log.info(mailResult.toString());
    }

    private Map<String, String> getContentTemplate(LeasePropertyNotificationConfiguration leasePropertyNotificationConfiguration,
                                                   List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries) {
        leaseExpiryPropertySummaries.sort(Comparator.comparingLong(LeaseExpiryPropertySummary::getDaysLeft));
        Map<String, String> contentTemplate = new HashMap<>();
        contentTemplate.put("DAYS", String.valueOf(leasePropertyNotificationConfiguration.getDays()));
        String html = createHTMLTable(leaseExpiryPropertySummaries);
        contentTemplate.put("TABLE", html);
        return contentTemplate;
    }

    private String createHTMLTable(List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<style>")
                .append("th { padding: 15px; text-align: center; } td { text-align: center; }")
                .append("</style>")
                .append("<table style=\"width:100%\">")
                .append(TR)
                .append(TH).append("PROPERTY").append(_TH)
                .append(TH).append("TENANT").append(_TH)
                .append(TH).append("DAYS").append(_TH)
                .append(TH).append("EXP").append(_TH)
                .append(TR);
        ListUtils.emptyIfNull(leaseExpiryPropertySummaries).forEach(leaseExpiryPropertySummary -> {
            stringBuilder
                    .append(TR)
                    .append(TD).append(leaseExpiryPropertySummary.getProperty()).append(_TD)
                    .append(TD).append(leaseExpiryPropertySummary.getTenant()).append(_TD)
                    .append(TD).append(leaseExpiryPropertySummary.getDaysLeft()).append(_TD)
                    .append(TD).append(leaseExpiryPropertySummary.getExpiredDate()).append(_TD)
                    .append(_TR);
        });
        stringBuilder.append("</table>")
                .append("</body>")
                .append("</html>");
        return stringBuilder.toString();
    }

    private void showSummary(List<LeaseExpiryPropertySummary> leaseExpiryPropertySummaries) {
        leaseExpiryPropertySummaries.forEach(leaseExpiryPropertySummary -> log.info(leaseExpiryPropertySummary.toString()));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaseExpiryPropertySummary {
        private String property;
        private String tenant;
        private long daysLeft;
        private String expiredDate;
    }
}
