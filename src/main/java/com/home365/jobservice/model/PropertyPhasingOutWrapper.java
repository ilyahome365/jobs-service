package com.home365.jobservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
@Data
public class PropertyPhasingOutWrapper implements Serializable {
    private String propertyId;
    private String triggerDateAndTime;
    private Boolean isBill;
    @JsonIgnore
    private String businessAction;
}
