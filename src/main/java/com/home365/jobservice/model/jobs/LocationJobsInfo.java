package com.home365.jobservice.model.jobs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationJobsInfo {
    private String location;
    private List<JobInfo> jobsInfo;
}
