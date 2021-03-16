package com.home365.jobservice.model;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class PropertyPhasingOutWrapper implements Serializable {
    private String propertyId;
    private String triggerDateAndTime;
    private Boolean isBill;
}
