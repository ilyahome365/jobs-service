package com.home365.jobservice;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.model.RecipientMail;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.model.mail.MailResult;
import com.home365.jobservice.rest.KeyCloakService;
import com.home365.jobservice.rest.TenantFeignService;
import com.home365.jobservice.service.JobLogService;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.service.OwnerNotificationsService;
import com.home365.jobservice.service.TransactionsService;
import com.home365.jobservice.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
@ActiveProfiles("test")
class JobServiceApplicationTests {
    @Autowired
    JobLogService jobLogService;
    @Autowired
    LateFeeJobServiceImpl lateFeeJobService;
    @Autowired
    DueDateNotificationServiceImpl dueDateNotificationService;
    @Autowired
    LeaseUpdatingServiceImpl leaseUpdatingService;
    @Autowired
    private TransactionsService transactionsService;
    @Autowired
    RecurringServiceImpl recurringService;
    @Autowired
    TenantFeignService tenantServiceExternal;
    @Autowired
    KeyCloakService keyCloakService;
    @Autowired
    PhaseOutPropertyServiceImpl phaseOutPropertyService;

    @Autowired
    OwnerNotificationsService ownerNotificationsService;

    @Autowired
    MailService mailService;

    @Autowired
    AppProperties appProperties;
//
//    @Test
//    public void lateFeeLVTest() throws Exception {
//        lateFeeJobService.execute("F90E128A-CD00-4DF7-B0D0-0F40F80D623A");
//    }
//
//    @Test
//    public void lateFeeATTest() throws Exception {
//        lateFeeJobService.execute("F90E128A-CD00-4DF7-B0D0-0F40F80D624A");
//    }
//
//    @Test
//    public void dueDateNotificationTest() throws Exception {
//        dueDateNotificationService.sendNotificationForDueDateTenants("F90E128A-CD00-4DF7-B0D0-0F40F80D623A");
//    }
//
//    @Test
//    public void leaseUpdateTest() throws Exception {
//        leaseUpdatingService.execute("F90E128A-CD00-4DF7-B0D0-0F40F80D623A");
//    }
//
//    @Test
//    public void recurringChargesTestLV() throws Exception {
//        recurringService.createTransactionsForRecurringCharges("F90E128A-CD00-4DF7-B0D0-0F40F80D623A");
//    }
//
//    @Test
//    public void recurringChargesTestAT() throws Exception {
//        recurringService.createTransactionsForRecurringCharges("F90E128A-CD00-4DF7-B0D0-0F40F80D624A");
//    }
//
//    @Test
//    public void phaseOutProperty() {
//        phaseOutPropertyService.executeJob("F90E128A-CD00-4DF7-B0D0-0F40F80D623A");
//    }
//
//    @Test void ownerNotificationTest(){
//        ownerNotificationsService.createOwnerNotification("F90E128A-CD00-4DF7-B0D0-0F40F80D623A");
//    }


    @Test
    @Disabled
    void testMail(){
        MailDetails mailDetails = new MailDetails();

        mailDetails.setFrom(appProperties.getMailSupport());
        mailDetails.setSubject("test");
        mailDetails.setTemplateName("job-executor-result");
        List<RecipientMail> recipientMails = ListUtils
                .emptyIfNull(appProperties.getJobExecutorMailToEmail())
                .stream()
                .map(email -> {
                    RecipientMail recipientMail = new RecipientMail();
                    recipientMail.setName(appProperties.getJobExecutorMailToName());
                    recipientMail.setEmail(email);
                    return recipientMail;
                }).collect(Collectors.toList());
        mailDetails.setRecipients(recipientMails);
        Map<String, String> stringStringMap = new HashMap<>();
        mailDetails.setContentTemplate(stringStringMap);
        MailResult mailResult = mailService.sendMail(mailDetails);
        log.info("Result : {} ", mailResult);
    }
}
