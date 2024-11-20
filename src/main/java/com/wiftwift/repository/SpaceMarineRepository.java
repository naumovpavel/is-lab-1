package com.wiftwift.repository;

import com.wiftwift.dto.CategoryCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import com.wiftwift.entity.SpaceMarine;

import java.util.List;

public interface SpaceMarineRepository extends JpaRepository<SpaceMarine, Integer> {
    
    Page<SpaceMarine> findByChapter_Id(int chapterId, Pageable pageable);

    List<SpaceMarine> findByChapter_Id(int chapterId);

    Page<SpaceMarine> findByNameContaining(String nameSubstring, Pageable pageable);

    List<SpaceMarine> findByCoordinates_Id(int coordinateID);

    @Query("SELECT AVG(sm.health) FROM SpaceMarine sm")
    Double calculateAverageHealth();

    @Query("SELECT DISTINCT sm.health FROM SpaceMarine sm")
    List<Long> findUniqueHealthValues();

    @NonNull
    Page<SpaceMarine> findAll(@NonNull Pageable pageable);
}
