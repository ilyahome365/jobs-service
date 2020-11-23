package com.home365.jobservice.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JobConfiguration {

    @Id
    private long id;

    private String cron;

    private String configurationJson;
}
