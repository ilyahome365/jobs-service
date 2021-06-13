package com.home365.jobservice.model;


import com.home365.jobservice.entities.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "AUDIT_EVENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent  implements Serializable,Cloneable {
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

    @Column
    private LocalDateTime createdOn = LocalDateTime.now(ZoneOffset.UTC);

    @Column
    private LocalDateTime updatedOn = LocalDateTime.now(ZoneOffset.UTC);

    @Column
    private String comment;


}
