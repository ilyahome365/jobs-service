package com.home365.jobservice.controller;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.service.impl.ApplicationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/jobs")
public class JobsController {

    private final ApplicationServiceImpl jobsService;

    public JobsController(ApplicationServiceImpl jobsService) {
        this.jobsService = jobsService;
    }

    @GetMapping("/test-jobs")
    public ResponseEntity<Boolean> getTest() {
        log.info("Enter to test jobs");
        return ResponseEntity.ok(true);
    }

    @GetMapping("/change-bill-state")
    public ResponseEntity<List<Transactions>> changeBillStates() {
        return ResponseEntity.ok(this.jobsService.pendingStatusChange());
    }

    @GetMapping("/late-fee")
    public ResponseEntity<Object> startLateFeeJob() {
        return ResponseEntity.ok(jobsService.startLateFeeJob());
    }
}
