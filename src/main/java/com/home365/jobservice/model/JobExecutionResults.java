package com.home365.jobservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

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

    private String jobName;
    private String message;
    private String error;
    private String stackTrace;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String elapsedTime;

    public void setElapsedTime(Duration duration) {
        this.elapsedTime = duration.toHours() + " hours, " + duration.toMinutes() + " minutes, " + duration.toSeconds() + " seconds";
    }

    public boolean isSucceeded() {
        return StringUtils.isEmpty(error);
    }

    @Override
    public String toString() {
        return "startTime=" + startTime + '\n' +
                "endTime=" + endTime + '\n' +
                "elapsedTime='" + elapsedTime + '\n' +
                "is Succeeded='" + isSucceeded() + '\n' +
                "error='" + error + '\n' +
                "stackTrace='" + stackTrace;
    }

    @JsonIgnore
    public String getStartTimeBeautify() {
        return dateTimeFormatter.format(startTime);
    }

    @JsonIgnore
    public String getEndTimeBeautify() {
        return dateTimeFormatter.format(endTime);
    }
}
