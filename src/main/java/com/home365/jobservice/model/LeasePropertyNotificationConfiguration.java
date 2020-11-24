package com.home365.jobservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LeasePropertyNotificationConfiguration {
    private int days;
    private String emailTemplateName;
    private String toMail;
    private String toName;
}
