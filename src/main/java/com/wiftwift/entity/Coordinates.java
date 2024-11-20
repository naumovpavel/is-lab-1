package com.wiftwift.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Entity
@Data
@Table(name = "coordinates")
public class Coordinates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;

    @Column(name = "x", nullable = false)
    @Max(value = 1000, message = "Максимальное значение поля x 1000")
    private Integer x;

    @Column(name = "y", nullable = false)
    private Double y;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
