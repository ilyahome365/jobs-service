package com.home365.jobservice.utils;

import com.home365.jobservice.model.JobExecutionResults;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public  class Templates {



    public static Map<String, String> getJobsContentTemplate(JobExecutionResults jobExecutionResults) {
        Map<String, String> contentTemplate = new HashMap<>();
        contentTemplate.put("START_TIME", jobExecutionResults.getStartTimeBeautify());
        contentTemplate.put("END_TIME", jobExecutionResults.getEndTimeBeautify());
        contentTemplate.put("ELAPSED_TIME", jobExecutionResults.getElapsedTime());
        contentTemplate.put("JOB_RESULT", StringUtils.isEmpty(jobExecutionResults.getMessage()) ? "" : jobExecutionResults.getMessage());
        contentTemplate.put("ERROR", StringUtils.isEmpty(jobExecutionResults.getError()) ? "" : jobExecutionResults.getError());
        contentTemplate.put("STACK_TRACE", StringUtils.isEmpty(jobExecutionResults.getStackTrace()) ? "" : jobExecutionResults.getStackTrace());
        return contentTemplate;
    }
}
