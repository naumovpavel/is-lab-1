package com.wiftwift.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.wiftwift.entity.Chapter;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Integer> {
    Page<Chapter> findByNameContaining(String nameSubstring, Pageable pageable);
    Page<Chapter> findByWorldContaining(String worldSubstring, Pageable pageable);
}
