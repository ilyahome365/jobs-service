package com.home365.jobservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LeasePropertyNotificationConfiguration {
    private int days;
    private String emailTemplateName;
    private String toName;
    private List<String> toMail;
}
