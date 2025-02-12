package com.wiftwift.entity;

import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "import_attemts")
public class ImportAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private boolean isAccepted;

    private int newObjectsCounter;
    @Column(name = "creation_date", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();
}
