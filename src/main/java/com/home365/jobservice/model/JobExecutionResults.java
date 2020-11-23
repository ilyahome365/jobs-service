package com.home365.jobservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExecutionResults implements Serializable {
    private boolean isSucceeded;
    private String message;
    private String error;
    private String stackTrace;
}
