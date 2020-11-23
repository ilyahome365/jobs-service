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
    private int days = 60;
    private String emailTemplateName = "notification-lease-expiry-date";
    private String toMail = "shlomo@home365.co";
    private String toName = "Shlomo";
}
