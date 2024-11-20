package com.wiftwift.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Date;

@Entity
@Data
@Table(name = "space_marines")
public class SpaceMarine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "id", nullable = false, unique = true)
    private int id;

    @Column(name = "name", nullable = false)
    @NotNull(message = "Поле name не может быть null")
    @Size(min = 1, message = "Поле name не может быть пустой")
    private String name;

    @Column(name = "creation_date", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

    @ManyToOne(optional = false)
    @JoinColumn(name = "coordinates_id", nullable = false)
    @NotNull(message = "Поле coordinates не может быть null")
    private Coordinates coordinates;

    @ManyToOne(optional = true)
    @JoinColumn(name = "chapter_id", nullable = true)
    private Chapter chapter;

    @Column(name = "health", nullable = false)
    @NotNull(message = "Поле health не может быть null")
    @Min(value = 0, message = "Значение поля health должно быть больше 0")
    private Long health;

    @Column(name = "height", nullable = true)
    @NotNull(message = "Поле height не может быть null")
    private Integer height;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    @NotNull(message = "Поле category не может быть null")
    private AstartesCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "weapon_type", nullable = false)
    @NotNull(message = "Поле weaponType не может быть null")
    private Weapon weaponType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
