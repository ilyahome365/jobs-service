package com.home365.jobservice.controller;

import com.home365.jobservice.config.AppConfiguration;
import com.home365.jobservice.model.JobInfo;
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
    public ResponseEntity<Object> restartJob() {
        List<JobInfo> jobs = appConfiguration.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @DeleteMapping("/{jobName}")
    public ResponseEntity<Object> removeJob(@PathVariable("jobName") String jobName) {
        appConfiguration.removeJob(jobName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/start-all")
    public ResponseEntity<Object> startAll() {
        appConfiguration.startAll();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/start/{jobName}")
    public ResponseEntity<Object> start(@PathVariable("jobName") String jobName) {
        appConfiguration.startJob(jobName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stop-all")
    public ResponseEntity<Object> stopAll() {
        appConfiguration.stopAll();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stop/{jobName}")
    public ResponseEntity<Object> stop(@PathVariable("jobName") String jobName) {
        appConfiguration.stopJob(jobName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/restart")
    public ResponseEntity<Object> restartAll() {
        appConfiguration.restartAll();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/restart/{jobName}")
    public ResponseEntity<Object> restartJob(@PathVariable("jobName") String jobName) {
        appConfiguration.restartJob(jobName);
        return ResponseEntity.ok().build();
    }
}
