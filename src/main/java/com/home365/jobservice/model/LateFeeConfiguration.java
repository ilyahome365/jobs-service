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
    private int lateFeeRetro;
    private boolean fix;
    private double feeAmount ;

    public double getFeeAmount() {
        if(fix){
            return feeAmount;
        }else{
            return feeAmount / 100;
        }
    }
}
