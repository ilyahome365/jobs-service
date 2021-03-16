package com.home365.jobservice.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class PropertyPhasingOutWrapper {
    private String propertyId;
    private LocalDate triggerDateAndTime;
}
