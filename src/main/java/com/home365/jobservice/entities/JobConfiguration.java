package com.home365.jobservice.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "JobConfiguration")
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JobConfiguration {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "cron")
    private String cron;

    @Column(name = "configurationJson")
    private String configurationJson;
}
