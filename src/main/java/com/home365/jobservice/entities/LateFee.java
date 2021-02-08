package com.home365.jobservice.entities;

import lombok.Data;

@Data
public class LateFee {
    private Float accountManagerPercentage;
    private String pmAccount;
    private Float ownerPercentage;
}
