package com.home365.jobservice.model;

import lombok.Data;

import java.util.List;

@Data
public class AccountRequest {
    private List<String> ids;
}
