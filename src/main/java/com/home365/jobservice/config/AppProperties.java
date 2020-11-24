package com.home365.jobservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "home365")
@Getter
@Setter
public class AppProperties {
    private Boolean sendMailOnJobExecutorFail;
    private Long trashHold;
    private String mandrillApiKey;
    private String mailSupport;
    private String jobExecutorMailToName;
    private String jobExecutorMailToEmail;
}
