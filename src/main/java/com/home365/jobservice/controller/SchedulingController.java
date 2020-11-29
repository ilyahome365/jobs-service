package com.home365.jobservice.controller;

import com.home365.jobservice.config.AppConfiguration;
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

    @DeleteMapping("/{location}/{jobName}")
    public ResponseEntity<Object> remove(@PathVariable("location") String location, @PathVariable("jobName") String jobName) {
        appConfiguration.removeJob(location, jobName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/start-all")
    public ResponseEntity<Object> startAll() {
        appConfiguration.startAll();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/start/{location}/{jobName}")
    public ResponseEntity<Object> start(@PathVariable("location") String location, @PathVariable("jobName") String jobName) {
        appConfiguration.startJob(location, jobName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stop-all")
    public ResponseEntity<Object> stopAll() {
        appConfiguration.stopAll();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stop/{location}/{jobName}")
    public ResponseEntity<Object> stop(@PathVariable("location") String location, @PathVariable("jobName") String jobName) {
        appConfiguration.stopJob(location, jobName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/restart")
    public ResponseEntity<Object> restartAll() {
        appConfiguration.restartAll();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/restart/{location}/{jobName}")
    public ResponseEntity<Object> restart(@PathVariable("location") String location, @PathVariable("jobName") String jobName) {
        appConfiguration.restartJob(location, jobName);
        return ResponseEntity.ok().build();
    }
}
