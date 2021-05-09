package com.home365.jobservice.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MailWrapper {

    String from;
    String subject;
    List<RecipientMail> recipients;
    Map<String, String> contentTemplate;
    String templateName;
    String attachmentPdf;
}
