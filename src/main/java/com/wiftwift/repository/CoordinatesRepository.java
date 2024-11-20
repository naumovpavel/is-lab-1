package com.wiftwift.repository;

import com.wiftwift.entity.Chapter;
import com.wiftwift.entity.Coordinates;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoordinatesRepository extends JpaRepository<Coordinates, Integer> {
}
