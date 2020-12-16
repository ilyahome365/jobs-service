package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.RecipientMail;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.model.mail.MailResult;
import com.home365.jobservice.service.DueDateNotificationService;
import com.home365.jobservice.service.MailService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DueDateNotificationServiceImpl implements DueDateNotificationService {
    private final JdbcTemplate jdbcTemplate;
    private final AppProperties appProperties;
    private final MailService mailService;

    public DueDateNotificationServiceImpl(JdbcTemplate jdbcTemplate, AppProperties appProperties, MailService mailService) {
        this.jdbcTemplate = jdbcTemplate;
        this.appProperties = appProperties;
        this.mailService = mailService;

    }

    @Override
    public JobExecutionResults sendNotificationForDueDateTenants(String locationId) {
        String query = "select ChargeAccountId, max(DueDate) MaxDueDate, caeb.new_contactid, c.FullName, c.EMailAddress1  from Transactions tr\n" +
                "inner join New_contactaccountExtensionBase caeb on caeb.new_accountid = tr.ChargeAccountId\n" +
                "inner join Contact c on c.ContactId = caeb.new_contactid\n" +
                "where ChargeAccountId in (\n" +
                "select a.AccountId\n" +
                "from Contact c\n" +
                "         inner join New_contactaccountExtensionBase ca on ca.new_contactid = c.ContactId\n" +
                "         inner join dbo.New_contactaccountBase cab on cab.New_contactaccountId=ca.New_contactaccountId\n" +
                "         inner join dbo.AccountExtensionBase a on a.AccountId=ca.New_AccountId\n" +
                "where cab.statuscode=1 and a.New_status in(1,4,6)\n" +
                "    and a.New_BusinessType = 10\n" +
                ") and status in ('readyForPayment') group by ChargeAccountId, caeb.new_contactid, c.FullName, c.EMailAddress1";

        List<Map<String, Object>> tenantChargesList = jdbcTemplate.queryForList(query);

        tenantChargesList.forEach(entry -> sendDueDateNotification((Timestamp) entry.get("MaxDueDate"), (String) entry.get("FullName"), (String) entry.get("EMailAddress1")));

        return JobExecutionResults.builder().build();
    }

    private void sendDueDateNotification(Timestamp maxDueDate, String fullName, String eMailAddress1) {

        Calendar calendar = Calendar.getInstance();
        double lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        double currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        double daysLeft = lastDay - currentDay;

        if(daysLeft == 5) {

        } else if(currentDay == 1 || currentDay == 6) {

        } else if (currentDay != 3 && currentDay % 3 == 0 && currentDay < lastDay - 6) {

        }

        MailDetails mailDetails = new MailDetails();
        mailDetails.setFrom(appProperties.getMailSupport());
        mailDetails.setSubject("Your payment is required");
        mailDetails.setTemplateName("duedate-payment-notification");
        mailDetails.setContentTemplate(getContentTemplate(fullName, maxDueDate));
        mailDetails.setRecipients(Collections.singletonList(new RecipientMail(
                fullName,
                "shauly@home365.co"
        )));

        MailResult mailResult = mailService.sendMail(mailDetails);
    }

    private Map<String, String> getContentTemplate(String fullName, Timestamp maxDueDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Map<String, String> contentTemplate = new HashMap<>();
        contentTemplate.put("TENANT_NAME", fullName);
        contentTemplate.put("PAYMENT_DUE", sdf.format(maxDueDate));
        contentTemplate.put("LINK_URL", "https://pmcrm.home365.co/pages/login");
        return contentTemplate;
    }
}
