package com.wiftwift.repository;

import com.wiftwift.entity.Coordinates;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoordinatesRepository extends JpaRepository<Coordinates, Integer> {
}
