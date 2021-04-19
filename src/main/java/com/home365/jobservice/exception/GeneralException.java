package com.home365.jobservice.exception;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GeneralException extends Exception {

    private String message;

    private String methodName;

}
