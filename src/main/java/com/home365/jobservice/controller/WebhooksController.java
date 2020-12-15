package com.home365.jobservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhooks")
public class WebhooksController {

    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/cash-payment-webhook")
    public ResponseEntity<String> cashPaymentWebhook(@RequestBody Object request) {
        try {
            log.info("Post cash_payment_webhook payload {}", mapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        } finally {
            return ResponseEntity.ok("success");
        }
    }
}
