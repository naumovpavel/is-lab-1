package com.wiftwift.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.wiftwift.entity.*;

public interface ImportAttemptsRepository  extends JpaRepository<ImportAttempt, Long>{
    
    Page<ImportAttempt> findByOwner_Id(Long ownerId, Pageable pageable);
    Page<ImportAttempt> findAll(Pageable pageable);
}
