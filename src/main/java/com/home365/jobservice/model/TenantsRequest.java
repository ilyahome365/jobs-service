package com.home365.jobservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantsRequest {



    private List<TenantDetails> tenantDetails;
    private LeaseDetails leaseDetails;
    private String propertyId;

}
