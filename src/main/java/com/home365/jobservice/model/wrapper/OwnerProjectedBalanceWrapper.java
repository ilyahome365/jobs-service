package com.home365.jobservice.model.wrapper;

import lombok.*;

import java.util.Map;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class OwnerProjectedBalanceWrapper {
    // the key is accountId and the value is the projected balance
    private Map<String, Double> ownerToProjectedBalance;
}
