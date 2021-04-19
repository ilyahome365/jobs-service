package com.home365.jobservice.entities;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Table(name = "JobLog")
@Data
public class JobLog {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String jobName;
    private LocalDate lastRun;
    private String comments;
    private String status;
    private Timestamp date;
}
