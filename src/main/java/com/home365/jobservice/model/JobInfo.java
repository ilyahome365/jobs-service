package com.home365.jobservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobInfo {
    private boolean active;
    private String taskName;
    private String crone;
}
