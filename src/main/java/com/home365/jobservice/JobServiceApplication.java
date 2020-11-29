package com.home365.jobservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobServiceApplication.class, args);
    }
}
