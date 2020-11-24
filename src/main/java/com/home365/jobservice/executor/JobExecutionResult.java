package com.home365.jobservice.executor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionResult {
    private boolean succeeded;
    private String message;
    private String error;
}
