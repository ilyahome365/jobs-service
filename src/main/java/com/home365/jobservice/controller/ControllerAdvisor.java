package com.home365.jobservice.controller;


import com.home365.jobservice.exception.ErrorDetails;
import com.home365.jobservice.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = GeneralException.class)
    public ResponseEntity<Object> handleGeneralException(GeneralException ex, WebRequest request) {
        log.info(ex.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        super.handleMethodArgumentNotValid(ex, headers, status, request);

        Map<String, ObjectError> errorsMap = ex
                .getBindingResult()
                .getAllErrors()
                .stream()
                .collect(Collectors.toMap(objectError -> ((FieldError) objectError).getField(), objectError -> objectError));

        String errorMassage = "MethodArgumentNotValidException: " + errorsMap.keySet()
                .stream()
                .map(key -> key + "=" + errorsMap.get(key))
                .collect(Collectors.joining(", ", "{", "}"));

        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                errorMassage,
                request.getDescription(false)
        );
        log.info(errorMassage);
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
}
