package com.home365.jobservice.service.impl;

import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.RecipientMail;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.service.MailService;
import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessageStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MailServiceImpl implements MailService {

    private final AppProperties appProperties;
    private final MandrillApi mandrillApi;

    public MailServiceImpl(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.mandrillApi = new MandrillApi(appProperties.getMandrillApiKey());
    }

    @Override
    public JobExecutionResults sendMail(MailDetails mailDetails) {
        JobExecutionResults jobExecutionResults = new JobExecutionResults();

        List<MandrillMessage.Recipient> recipientList = createRecipients(mailDetails.getRecipients());

        List<MandrillMessage.MessageContent> messageContents = ListUtils.emptyIfNull(mailDetails.getMessageContents())
                .stream()
                .map(messageContent -> createMessageContent(
                        messageContent.getContent(),
                        messageContent.getType(),
                        messageContent.getName()
                ))
                .collect(Collectors.toList());

        MandrillMessage mandrillMessage = createMessage(
                mailDetails.getFrom(),
                mailDetails.getSubject(),
                recipientList,
                createMergeVars(recipientList, mailDetails.getContentTemplate()),
                messageContents
        );

        try {
            MandrillMessageStatus[] messageStatusReports = mandrillApi.messages().sendTemplate(
                    mailDetails.getTemplateName(),
                    mailDetails.getContentTemplate(),
                    mandrillMessage,
                    false
            );

            StringBuilder stringBuilder = new StringBuilder();
            Arrays.stream(messageStatusReports).forEach(mandrillMessageStatus -> {
                stringBuilder.append(String.format("Mandrill Message Details Email [%s], Status [%s], Reject Reason [%s]",
                        mandrillMessageStatus.getEmail(),
                        mandrillMessageStatus.getStatus(),
                        mandrillMessageStatus.getRejectReason()
                        )
                );
                stringBuilder.append("\n");
            });
            jobExecutionResults.setSucceeded(true);
            jobExecutionResults.setMessage(stringBuilder.toString());
        } catch (Exception exception) {
            jobExecutionResults.setSucceeded(false);
            jobExecutionResults.setError(exception.getMessage());
            jobExecutionResults.setStackTrace(Arrays.toString(exception.getStackTrace()));
        }
        return jobExecutionResults;
    }

    private List<MandrillMessage.Recipient> createRecipients(List<RecipientMail> recipients) {
        return recipients
                .stream()
                .map(recipientMail -> {
                    MandrillMessage.Recipient recipient = new MandrillMessage.Recipient();
                    recipient.setName(recipientMail.getName());
                    recipient.setEmail(recipientMail.getEmail());
                    return recipient;
                })
                .collect(Collectors.toList());
    }

    private MandrillMessage createMessage(String from,
                                          String subject,
                                          List<MandrillMessage.Recipient> recipients,
                                          List<MandrillMessage.MergeVarBucket> mergeVarBuckets,
                                          List<MandrillMessage.MessageContent> messageContents) {
        MandrillMessage message = new MandrillMessage();
        message.setSubject(subject);
        message.setFromEmail(from);
        message.setTo(recipients);
        message.setMerge(true);
        message.setMergeLanguage("mailchimp");
        message.setMergeVars(mergeVarBuckets);
        message.setAttachments(messageContents);
        return message;
    }

    private MandrillMessage.MessageContent createMessageContent(String content,
                                                                String contentType,
                                                                String contentName) {
        MandrillMessage.MessageContent messageContent = new MandrillMessage.MessageContent();
        messageContent.setContent(content);
        messageContent.setType(contentType);
        messageContent.setName(contentName);
        return messageContent;
    }

    private List<MandrillMessage.MergeVarBucket> createMergeVars(List<MandrillMessage.Recipient> recipients,
                                                                 Map<String, String> vars) {
        List<MandrillMessage.MergeVarBucket> mergeVarBuckets = new ArrayList<>();
        for (MandrillMessage.Recipient recipient : recipients) {
            MandrillMessage.MergeVarBucket mergeVarBucket = new MandrillMessage.MergeVarBucket();
            mergeVarBucket.setRcpt(recipient.getEmail());
            MandrillMessage.MergeVar[] mergeVars = new MandrillMessage.MergeVar[vars.size()];
            int index = 0;
            for (Map.Entry<String, String> pair : vars.entrySet()) {
                MandrillMessage.MergeVar mergeVar = new MandrillMessage.MergeVar();
                mergeVar.setName(pair.getKey());
                mergeVar.setContent(pair.getValue());
                mergeVars[index] = mergeVar;
                index = index + 1;

            }
            mergeVarBucket.setVars(mergeVars);
            mergeVarBuckets.add(mergeVarBucket);
        }
        return mergeVarBuckets;
    }
}
