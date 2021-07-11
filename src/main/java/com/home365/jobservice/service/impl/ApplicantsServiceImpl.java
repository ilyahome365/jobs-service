package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.config.Constants;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.executor.JobExecutorImpl;
import com.home365.jobservice.model.PropertyApplicationsNotified;
import com.home365.jobservice.model.RecipientMail;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.model.mail.MailResult;
import com.home365.jobservice.rest.ApplicantExternalService;
import com.home365.jobservice.rest.model.ApplicationRequest;
import com.home365.jobservice.rest.model.PropertyApplicationResponse;
import com.home365.jobservice.rest.model.enums.ApplicationStatus;
import com.home365.jobservice.service.ApplicantsService;
import com.home365.jobservice.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ApplicantsServiceImpl extends JobExecutorImpl implements ApplicantsService {

    private final ApplicantExternalService applicantExternalService;
    private final MailService mailService;
    private final AppProperties appProperties;

    protected ApplicantsServiceImpl(AppProperties appProperties, MailService mailService, ApplicantExternalService applicantExternalService, MailService mailService1, AppProperties appProperties1) {
        super(appProperties, mailService);
        this.applicantExternalService = applicantExternalService;
        this.mailService = mailService1;
        this.appProperties = appProperties1;
    }

    @Override
    public String notifyApplicantsWhoDidntFinishTheFlow() {
        ApplicationRequest applicationRequest = new ApplicationRequest();
        List<String> name = List.of(ApplicationStatus.savedAsCandidate.name(),
                ApplicationStatus.decisionPending.name(),
                ApplicationStatus.interested.name(),
                ApplicationStatus.inScreening.name());
        applicationRequest.setApplicationStatus(name);
        LocalDateTime today = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime yesterday = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);

        applicationRequest.setCreationDate(Timestamp.valueOf(yesterday));
        applicationRequest.setUntilDate(Timestamp.valueOf(today));
        List<PropertyApplicationsNotified > propertyApplicationsNotifieds = new ArrayList<>();
        try {
            List<PropertyApplicationResponse> propertyApplicationsByCreationDateAndStatuses = applicantExternalService.getPropertyApplicationsByCreationDateAndStatuses(applicationRequest);
            propertyApplicationsByCreationDateAndStatuses.forEach(propertyApplicationResponse -> {
                MailDetails mailDetails = new MailDetails();
                mailDetails.setTemplateName("applicant-didn-t-apply");
                mailDetails.setSubject("Finish you application with Home365");
                RecipientMail recipientMail = new RecipientMail();
                recipientMail.setName(propertyApplicationResponse.getFirstName());
                recipientMail.setEmail(propertyApplicationResponse.getEmail());
                mailDetails.setRecipients(List.of(recipientMail));
                Map<String, String> contentMap = new HashMap<>();
                contentMap.put("APPLICANT_FIRST_NAME", propertyApplicationResponse.getFirstName());
                mailDetails.setContentTemplate(contentMap);
                mailDetails.setFrom(appProperties.getMailSupport());
                MailResult mailResult = mailService.sendMail(mailDetails);
                log.info("MAIL Result : {} ", mailResult);
               PropertyApplicationsNotified propertyApplicationsNotified = new PropertyApplicationsNotified();
               propertyApplicationsNotified.setApplicationId(propertyApplicationResponse.getApplicationId());
               propertyApplicationsNotified.setEmail(propertyApplicationResponse.getEmail());
               propertyApplicationsNotifieds.add(propertyApplicationsNotified);
            });

        } catch (GeneralException e) {
            log.error("ERROR : {}", e.getMessage());
        }
        return "Notification has been sent to : " + propertyApplicationsNotifieds.toString();
    }

    @Override
    protected String getJobName() {
        return Constants.REMINDER_APPLICANT_FINISH_THE_FLOW;
    }

    @Override
    protected String execute(String locationId) throws Exception {

        return this.notifyApplicantsWhoDidntFinishTheFlow();
    }
}
