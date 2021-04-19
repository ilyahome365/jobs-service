package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.config.Constants;
import com.home365.jobservice.entities.projection.IOwnerRentNotification;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.model.RecipientMail;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.model.mail.MailResult;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.service.OwnerNotificationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OwnerNotificationsServiceImpl extends JobExecutorImpl implements OwnerNotificationsService {

    private final TransactionsServiceImpl transactionsService;

    public OwnerNotificationsServiceImpl(AppProperties appProperties, MailService mailService, TransactionsServiceImpl transactionsService) {
        super(appProperties, mailService);
        this.transactionsService = transactionsService;
    }

    @Override
    public String createOwnerNotification(String locationId) {
        log.info("start owner notification for location : {} ", locationId);

        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(10);
        List<IOwnerRentNotification> transactionsForOwnerRent = transactionsService.getTransactionsForOwnerRent(locationId,
                startDate.toString(), endDate.toString()).stream().filter(Objects::nonNull).collect(Collectors.toList());

        StringBuilder propertiesToSendNotification = new StringBuilder();
        Map<String, List<IOwnerRentNotification>> ownerNotificationByContact = transactionsForOwnerRent.stream()
                .collect(Collectors.groupingBy(IOwnerRentNotification::getContactId));
        for (Map.Entry<String, List<IOwnerRentNotification>> entry : ownerNotificationByContact.entrySet()) {
            List<IOwnerRentNotification> ownerRentNotifications = entry.getValue();
            String properties = ownerRentNotifications.stream().map(IOwnerRentNotification::getAddress).collect(Collectors.joining("\n"));
            IOwnerRentNotification ownerRentNotification = ownerRentNotifications.get(0);
            createMailAndSend(ownerRentNotification, properties);
            propertiesToSendNotification.append(properties);
        }
        return String.format("properties to send for them notification :  %s", propertiesToSendNotification.toString());
    }

    private void createMailAndSend(IOwnerRentNotification ownerRentNotification, String properties) {
        MailDetails mailDetails = new MailDetails();
        mailDetails.setFrom(appProperties.getMailSupport());
        mailDetails.setContentTemplate(createOwnerNotificationContentTemplate(ownerRentNotification, properties));
        RecipientMail recipientMail = RecipientMail.builder()
                .name(ownerRentNotification.getFirstName() + " " + ownerRentNotification.getLastName())
                .email(ownerRentNotification.getEmail())
                .build();
        mailDetails.setRecipients(List.of(recipientMail));
        String emailSubject = "We are working to collect your rent";
        mailDetails.setSubject(emailSubject);
        mailDetails.setTemplateName("owner-rent-notification");
        MailResult mailResult = mailService.sendMail(mailDetails);
        log.info("Mail Result : {} ", mailResult);
    }

    private Map<String, String> createOwnerNotificationContentTemplate(IOwnerRentNotification ownerRentNotification, String properties) {
        Map<String, String> contentTemplate = new HashMap<>();
        contentTemplate.put("OWNER_NAME", ownerRentNotification.getFirstName() + " " + ownerRentNotification.getLastName());
        contentTemplate.put("PROPERTY_ADDRESS", properties);
        return contentTemplate;
    }




    @Override
    protected String getJobName() {
        return Constants.OWNER_RENT_NOTIFICATION;
    }

    @Override
    protected String execute(String locationId) {
        return createOwnerNotification(locationId);
    }
}
