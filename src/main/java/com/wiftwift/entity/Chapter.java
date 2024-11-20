package com.wiftwift.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;
import org.hibernate.annotations.Formula;

@Entity
@Data
@Table(name = "chapters")
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private int id;

    @Column(name = "name", nullable = false)
    @NotNull(message = "Поле name не может быть null")
    @Size(min = 1, message = "Поле name не может быть пустой")
    private String name;

    @Column(name = "world", nullable = false)
    @NotNull(message = "Поле world не может быть null")
    private String world;

    @Formula("(SELECT COUNT(sm.id) FROM space_marines sm WHERE sm.chapter_id = id)")
    private Integer marinesCount;


    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}