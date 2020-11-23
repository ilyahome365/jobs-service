package com.home365.jobservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LateFeeConfiguration {
    private int lateFeeRetro = -1;
    private double feeAmount = 5;

    public double getFeeAmountPercentage() {
        return feeAmount / 100;
    }
}
