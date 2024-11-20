package com.wiftwift.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; 

    @Column(nullable = false)
    private LocalDateTime submittedAt; 

    @Enumerated(EnumType.STRING)
    private RequestStatus status; 

    public enum RequestStatus {
        PENDING, ACCEPTED, REJECTED
    }
}
