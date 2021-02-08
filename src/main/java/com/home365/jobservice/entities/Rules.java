package com.home365.jobservice.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class Rules implements Serializable {

    private Double bankFee;
    private Double creditFee;
    @JsonProperty(value = "day_in_month_to_create_recurring")
    private Integer dayInmMonthToCreateRecurring;
    @JsonProperty(value = "logical_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate logicalDate;
    @JsonProperty(value = "cash_payment_max_value")
    private Double cashPaymentMaxValue;
    @JsonProperty(value = "cash_payment_available")
    private Boolean cashPaymentAvailable;
    @JsonProperty(value = "max_num_of_installments")
    private Integer maxNumOfInstallments;
    private Integer reversePaymentFine;
    private LateFee lateFees;

}
