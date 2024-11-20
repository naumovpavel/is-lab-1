package com.wiftwift.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wiftwift.entity.User;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findAllByRoles_Name(String roleName);
}

