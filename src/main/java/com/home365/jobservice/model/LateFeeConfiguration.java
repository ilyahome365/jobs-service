package com.home365.jobservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LateFeeConfiguration {
    private int lateFeeRetro;
    private boolean fix;
    private double feeAmount;
    private double maxFeeAmount;
    private List<String> categoryNames;
    private List<String> status;

    public double getFeeAmount() {
        if (fix) {
            return feeAmount;
        } else {
            return feeAmount / 100;
        }
    }
}
