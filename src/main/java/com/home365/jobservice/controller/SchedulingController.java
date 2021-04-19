package com.home365.jobservice.controller;

import com.home365.jobservice.config.AppConfiguration;
import com.home365.jobservice.model.jobs.JobOpsRequest;
import com.home365.jobservice.model.jobs.LocationJobsInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scheduling")
public class SchedulingController {


    @Autowired
    private AppConfiguration appConfiguration;

    @GetMapping("/")
    public ResponseEntity<Object> getAllJob() {
        List<LocationJobsInfo> jobs = appConfiguration.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @DeleteMapping("/")
    public ResponseEntity<Object> remove(@RequestBody JobOpsRequest jobOpsRequest) {
        appConfiguration.removeJob(jobOpsRequest.getLocation(), jobOpsRequest.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/start-all")
    public ResponseEntity<Object> startAll() {
        appConfiguration.startAll();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/start")
    public ResponseEntity<Object> start(@RequestBody JobOpsRequest jobOpsRequest) {
        appConfiguration.startJob(jobOpsRequest.getLocation(), jobOpsRequest.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stop-all")
    public ResponseEntity<Object> stopAll() {
        appConfiguration.stopAll();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop")
    public ResponseEntity<Object> stop(@RequestBody JobOpsRequest jobOpsRequest) {
        appConfiguration.stopJob(jobOpsRequest.getLocation(), jobOpsRequest.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/restart-all")
    public ResponseEntity<Object> restartAll() {
        appConfiguration.restartAll();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restart")
    public ResponseEntity<Object> restart(@RequestBody JobOpsRequest jobOpsRequest) {
        appConfiguration.restartJob(jobOpsRequest.getLocation(), jobOpsRequest.getName());
        return ResponseEntity.ok().build();
    }




}
