package com.home365.jobservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.config.Constants;
import com.home365.jobservice.entities.projection.IDueDateEntry;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.RecipientMail;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.model.mail.MailResult;
import com.home365.jobservice.repository.TransactionsRepository;
import com.home365.jobservice.service.DueDateNotificationService;
import com.home365.jobservice.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class DueDateNotificationServiceImpl extends JobExecutorImpl implements DueDateNotificationService {
    private final TransactionsRepository transactionsRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${tenant.login.url}")
    String tenantLoginUrl;

    public DueDateNotificationServiceImpl( AppProperties appProperties, MailService mailService, TransactionsRepository transactionsRepo) {
        super(appProperties, mailService);
        this.transactionsRepo = transactionsRepo;
    }

    @Override
    public JobExecutionResults sendNotificationForDueDateTenants(String locationId) {
        double currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        if (currentDay == 1 || currentDay == 6 || currentDay == 10 || currentDay == 15 || currentDay == 20 || currentDay == 25) {
            List<IDueDateEntry> tenantChargesList = transactionsRepo.getDueDateNotificationsByPmAccountId(locationId);

            tenantChargesList.forEach(entry -> sendDueDateNotification(entry.getMaxDueDate(), entry.getFirstName() + " " + entry.getLastName(), entry.getEMailAddress1(), entry.getTenantJson()));

            return JobExecutionResults.builder()
                    .message(String.format("Sent %s notification mails for location %s", tenantChargesList.size(), locationId))
                    .build();
        } else {
            return JobExecutionResults.builder()
                    .message(String.format("No need to send due date notifications for location %s", locationId))
                    .build();
        }
    }

    private void sendDueDateNotification(Timestamp maxDueDate, String fullName, String eMailAddress, String tenantJson) {
        if (StringUtils.isEmpty(eMailAddress)) {
            AtomicReference<String> eMailAddressAtomic = new AtomicReference();
            try {
                mapper.readTree(tenantJson).get(Constants.TENANT_DETAILS).forEach(e -> {
                    BooleanNode isContactPerson = (BooleanNode) e.get(Constants.CONTACT_PERSON);
                    if(isContactPerson.asBoolean()) {
                        eMailAddressAtomic.set(e.get("email").asText());
                    }
                });
            } catch (JsonProcessingException e) {
                log.error("Cannot parse json {}", tenantJson);
            }
            eMailAddress = eMailAddressAtomic.get();
        } if(!StringUtils.isEmpty(eMailAddress)) {
            MailDetails mailDetails = new MailDetails();
            mailDetails.setFrom(appProperties.getMailSupport());
            mailDetails.setSubject(Constants.PAYMENT_REMINDER);
            mailDetails.setTemplateName(Constants.DUEDATE_PAYMENT_NOTIFICATION);
            mailDetails.setContentTemplate(getContentTemplate(fullName, maxDueDate));
            List<RecipientMail> recipients = new ArrayList<>(Arrays.asList(new RecipientMail(fullName, eMailAddress)));
            mailDetails.setRecipients(recipients);

            MailResult mailResult = mailService.sendMail(mailDetails);
            log.info("Mail Result for {}: {}", fullName, mailResult.getError());
        } else {
            log.warn("Cannot send mail to {} due to missing email address", fullName);
        }
    }

    private Map<String, String> getContentTemplate(String fullName, Timestamp maxDueDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Map<String, String> contentTemplate = new HashMap<>();
        contentTemplate.put("TENANT_NAME", fullName);
        contentTemplate.put("PAYMENT_DUE", sdf.format(maxDueDate));
        contentTemplate.put("LINK_URL", tenantLoginUrl);
        return contentTemplate;
    }

    @Override
    protected String getJobName() {
        return "due-date-notification";
    }

    @Override
    protected String execute(String locationId) {
        return sendNotificationForDueDateTenants(locationId).getMessage();
    }
}
