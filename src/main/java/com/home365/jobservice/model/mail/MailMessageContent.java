package com.home365.jobservice.model.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MailMessageContent {
    private String content;

    // ex. application/pdf
    private String type;

    // ex. Monthly Statement Report.pdf
    private String name;
}
