package com.home365.jobservice.model.wrapper;


import com.home365.jobservice.model.enums.TenantStatus;
import lombok.Data;

@Data
public class TenantWrapper {
    private String accountId;
    private TenantStatus tenantStatus;
}
