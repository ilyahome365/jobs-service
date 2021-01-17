package com.home365.jobservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home365.jobservice.service.CashPaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/webhooks")
public class WebhooksController {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    CashPaymentService cashPaymentService;

    @PostMapping("/cash-payment-webhook")
    public ResponseEntity<String> cashPaymentWebhook(@RequestBody Object request) {
        try {
            log.info("Post cash_payment_webhook payload {}", mapper.writeValueAsString(request));
            cashPaymentService.handleCashPaymentWebhookResponse(request);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        } finally {
            return ResponseEntity.ok("success");
        }
    }
}
