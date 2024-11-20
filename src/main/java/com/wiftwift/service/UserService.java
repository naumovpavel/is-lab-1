package com.wiftwift.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wiftwift.entity.User;
import com.wiftwift.repository.UserRepository;

import java.util.Optional;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public List<User> getAllAdmins() {
        return userRepository.findAllByRoles_Name("ADMIN");
    }

    public boolean isAdmin(String username) {
        return userRepository.findAllByRoles_Name("ADMIN").stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
