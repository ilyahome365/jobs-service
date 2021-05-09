package com.home365.jobservice.service.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.MailWrapper;
import com.home365.jobservice.model.RecipientMail;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.utils.Templates;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MailServiceImpl implements MailService {

    private final AppProperties appProperties;

    AmazonSQSAsync amazonSQS;
    private QueueMessagingTemplate queueMessagingTemplate;
    private String sqsUrl;

    public MailServiceImpl(@Value("${aws.accessKey:@null}") String accessKey, @Value("${aws.secretKey:@null}")String secretKey,
                           @Value("${aws.region:@null}") String region, @Value("${sqs.url:@null}") String sqsUrl,
                           AppProperties appProperties) {
        if(!StringUtils.isEmpty(accessKey) && !StringUtils.isEmpty(secretKey)){
            AWSStaticCredentialsProvider awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
            amazonSQS = AmazonSQSAsyncClientBuilder.standard().withCredentials(awsStaticCredentialsProvider).build();
            this.queueMessagingTemplate = new QueueMessagingTemplate(amazonSQS);
        }
        this.sqsUrl = sqsUrl;
        this.appProperties = appProperties;
    }

    @Override
    public void sendMail(MailDetails mailDetails) {
        log.info("Send mail for {} ", mailDetails);
        MailWrapper mailWrapper = null;
        if(mailDetails != null){
            mailWrapper = new MailWrapper();
            mailWrapper.setTemplateName(mailDetails.getTemplateName());
            mailWrapper.setContentTemplate(mailDetails.getContentTemplate());
            mailWrapper.setRecipients(mailDetails.getRecipients());
            mailWrapper.setSubject(mailDetails.getSubject());
            mailWrapper.setFrom(mailDetails.getFrom());
        }
        if(amazonSQS != null && mailWrapper != null){
            queueMessagingTemplate.convertAndSend("home365NoCrmDevQueue",mailWrapper);
        }else{
            log.error("No sqs defined");
        }
    }

    @Override
    public void sendMailFromJobExecuteResults(JobExecutionResults jobExecutionResults, String from, List<String> recipientList, String templateName) {
        MailDetails mailDetails = new MailDetails();
        mailDetails.setFrom(from);
        mailDetails.setSubject(jobExecutionResults.getJobName());
        mailDetails.setTemplateName(templateName);
        List<RecipientMail> recipientMails = ListUtils
                .emptyIfNull(recipientList)
                .stream()
                .map(email -> {
                    RecipientMail recipientMail = new RecipientMail();
                    recipientMail.setName(appProperties.getJobExecutorMailToName());
                    recipientMail.setEmail(email);
                    return recipientMail;
                }).collect(Collectors.toList());
        mailDetails.setRecipients(recipientMails);
        mailDetails.setContentTemplate(Templates.getJobsContentTemplate(jobExecutionResults));
        sendMail(mailDetails);
    }

}
