package com.home365.jobservice.service;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.exception.PropertyNotFoundLocal;

public interface PayBillsService {
    String insurancePayBills(String locationId) throws PropertyNotFoundLocal, GeneralException;
}
