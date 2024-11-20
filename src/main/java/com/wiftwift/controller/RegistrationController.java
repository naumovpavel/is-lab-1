package com.wiftwift.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.wiftwift.entity.User;
import com.wiftwift.repository.UserRepository;

@Controller
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; 

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());  
        return "register";  
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user) {
        
        String passwordHash = passwordEncoder.encode(user.getPasswordHash());
        
        
        user.setPasswordHash(passwordHash);
        
        
        userRepository.save(user);
        return "redirect:/login";  
    }
}
