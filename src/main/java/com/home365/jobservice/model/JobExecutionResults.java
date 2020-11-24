package com.home365.jobservice.model;

import com.home365.jobservice.executor.JobExecutionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExecutionResults implements Serializable {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private String error;
    private String stackTrace;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String elapsedTime;
    private JobExecutionResult jobExecutionResult;

    public void setElapsedTime(Duration duration) {
        this.elapsedTime = duration.toHours() + " hours, " + duration.toMinutes() + " minutes, " + duration.toSeconds() + " seconds";
    }

    @Override
    public String toString() {
        return " startTime=" + startTime + '\n' +
                "endTime=" + endTime + '\n' +
                "elapsedTime='" + elapsedTime + '\n' +
                "error='" + error + '\n' +
                "stackTrace='" + stackTrace + '\n' +
                "jobExecutionResult=" + jobExecutionResult;
    }
}
