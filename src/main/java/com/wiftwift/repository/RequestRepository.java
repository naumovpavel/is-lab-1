package com.wiftwift.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wiftwift.entity.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByUserId(Long userId); 

}
