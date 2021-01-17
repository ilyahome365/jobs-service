package com.home365.jobservice.model.mail;

import com.home365.jobservice.model.RecipientMail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailDetails {
    private String from;
    private String subject;
    private String templateName;
    private List<RecipientMail> recipients;
    private Map<String, String> contentTemplate;
    private List<MailMessageContent> messageContents;
}
