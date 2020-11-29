package com.home365.jobservice.model.jobs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.TimeZone;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobInfo {
    private boolean active;
    private String taskName;
    private String cron;
    private String location;
    private TimeZone timeZone;
}
