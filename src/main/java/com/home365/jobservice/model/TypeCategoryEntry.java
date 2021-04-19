package com.home365.jobservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Entity
public class TypeCategoryEntry {

    @Column(insertable = false, updatable = false)
    String type;
    @Column(insertable = false, updatable = false)
    String category;
    @Column(insertable = false, updatable = false)
    String categoryId;
    @Column(insertable = false, updatable = false)
    String typeId;
    @Column(insertable = false, updatable = false)
    int categoryType;
    @Enumerated(EnumType.STRING)
    @Column(insertable = false, updatable = false)
    com.home365.jobservice.model.enums.TransferTo TransferTo;

    @JsonIgnore
    @EmbeddedId
    private TypeCategoryId id;

    @Transient
    String display;
}

