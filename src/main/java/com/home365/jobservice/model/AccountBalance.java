package com.home365.jobservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AccountBalance {
    private double balance;
    private List<PropertyBalanceDetails> propertiesBalanceDetails;
}
