package com.home365.jobservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantStatusChangeRequest {
    private String accountId;
    private String contactId;
    private String transactionId;
    private String categoryName;
    private int businessType;
    private Boolean createToPhaseOut;
}
