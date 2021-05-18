package com.home365.jobservice.controller;

import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.service.impl.ApplicationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/jobs")
public class JobsController {

    private final ApplicationServiceImpl jobsService;

    public JobsController(ApplicationServiceImpl jobsService) {
        this.jobsService = jobsService;
    }

    @GetMapping("/change-bill-state")
    public ResponseEntity<JobExecutionResults> changeBillStates(@RequestParam String locationId) {
        return ResponseEntity.ok(jobsService.startChangeBillStatusJob(locationId));
    }

    @GetMapping("/late-fee")
    public ResponseEntity<Object> startLateFeeJob(@RequestParam String locationId) {
        return ResponseEntity.ok(jobsService.startLateFeeJob(locationId));
    }

    @GetMapping("/create-transactions-for-recurring-charges")
    public ResponseEntity<JobExecutionResults> createTransactionsForRecurringCharges(@RequestParam String locationId) {
        return ResponseEntity.ok(jobsService.createTransactionsForRecurringCharges(locationId));
    }

    @GetMapping("/lease-property-notification")
    public ResponseEntity<Object> startLeasePropertyNotification(@RequestParam String locationId) {
        return ResponseEntity.ok(jobsService.startLeasePropertyNotification(locationId));
    }

    @GetMapping("/due-date-tenants-notification")
    public ResponseEntity<JobExecutionResults> dueDateTenantNotification(@RequestParam String locationId) {
        return ResponseEntity.ok(jobsService.dueDateTenantNotification(locationId));
    }

    @GetMapping("/lease-updating")
    public ResponseEntity<JobExecutionResults> leaseUpdating(@RequestParam String locationId) {
        return ResponseEntity.ok(jobsService.startLeaseUpdating(locationId));
    }

    @GetMapping("/phase-out-property")
    public ResponseEntity<JobExecutionResults> propertyPhaseOut(@RequestParam String locationId) {
        log.info("Start property phase out for locationId : {} ", locationId);
        return ResponseEntity.ok(jobsService.startPhaseOutProperty(locationId));
    }

    @GetMapping("/owner-notification")
    public ResponseEntity<JobExecutionResults> ownerNotification(@RequestParam String locationId) {
        log.info("Start property phase out for locationId : {} ", locationId);
        return ResponseEntity.ok(jobsService.startOwnerRentNotification(locationId));
    }

    @GetMapping("/activate-owners")
    public ResponseEntity<JobExecutionResults> activateOwners(){
        return ResponseEntity.ok(jobsService.activateOwners());
    }

    @GetMapping("/create-welcome-credit")
    public ResponseEntity<JobExecutionResults> createWelcomeCredit(){
        return ResponseEntity.ok(jobsService.createWelcomeCredit());
    }
}
