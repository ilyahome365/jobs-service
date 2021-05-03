package com.home365.jobservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "home365")
@Getter
@Setter
public class AppProperties {
    private Boolean sendMailOnJobExecutorFail;
    private Long trashHold;
    private String mandrillApiKey;
    private String mailSupport;
    private String portalUrl;
    private String jobExecutorMailToName;
    private List<String> jobExecutorMailToEmail;
    private String jobExecuteTemplateName;
}
