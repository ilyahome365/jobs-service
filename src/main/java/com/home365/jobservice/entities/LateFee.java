package com.home365.jobservice.entities;

import lombok.Data;

import java.io.Serializable;

@Data
public class LateFee implements Serializable {
    private Float accountManagerPercentage;
    private String pmAccount;
    private Float ownerPercentage;
}
