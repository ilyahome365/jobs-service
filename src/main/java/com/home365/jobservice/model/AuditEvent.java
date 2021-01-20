package com.home365.jobservice.model;


import com.home365.jobservice.entities.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AUDIT_EVENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {
    @Id
    String id;

    @Column
    String userId;

    @Column
    @Enumerated(EnumType.STRING)
    EntityType entityType;

    @Column
    String entityIdentifier;

    @Column
    String message;

    @CreationTimestamp
    private LocalDateTime createdOn;

    @UpdateTimestamp
    private LocalDateTime updatedOn;

    @Column
    private String comment;

}
