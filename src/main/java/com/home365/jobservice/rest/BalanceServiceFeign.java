package com.home365.jobservice.rest;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.PropertyPhasingOutWrapper;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface BalanceServiceFeign {

    @RequestLine("POST /bills/cancel-bills-by-property-and-dueDate")
    @Headers({"Authorization: Bearer {token}","Content-Type: application/json"})
    List<Integer> cancelBillsByPropertyAndDueDate(@Param("token") String token, PropertyPhasingOutWrapper propertyPhasingOutWrapper) throws GeneralException;
}
