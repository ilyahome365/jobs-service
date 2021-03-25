package com.home365.jobservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class PropertyBalanceDetails implements Serializable {
    String propertyId;
    double amount;
}
