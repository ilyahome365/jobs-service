package com.home365.jobservice.rest.model;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class ApplicationRequest {
    private List<String> applicationStatus;
    private Timestamp creationDate;
    private Timestamp untilDate;
}
