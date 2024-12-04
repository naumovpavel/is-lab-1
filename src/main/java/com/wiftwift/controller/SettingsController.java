package com.wiftwift.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.wiftwift.entity.Request;
import com.wiftwift.entity.Role;
import com.wiftwift.entity.User;
import com.wiftwift.service.RequestService;
import com.wiftwift.service.RoleService;
import com.wiftwift.service.UserService;

import java.util.Optional;

@Controller
public class SettingsController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/settings")
    public String settingsPage(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow();

        model.addAttribute("username", username);
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
        
        user.getRoles().forEach(role -> System.out.println(role.getName()));
        model.addAttribute("isAdmin", isAdmin);
        
        Optional<Request> userRequest = requestService.getUserRequest(user.getId());
        model.addAttribute("username", authentication.getName());
        model.addAttribute("userRequest", userRequest.orElse(null));

        return "settings";
    }

    @Transactional
    @PostMapping("/settings/request-admin-role")
    public String submitRequest(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow();

        boolean isAdmin = !userService.getAllAdmins().isEmpty();

        if (!isAdmin) {
            if (roleService.findByName("ADMIN").isEmpty()) {
                Role adminRole = new Role();
                adminRole.setName("ADMIN");
                roleService.save(adminRole);
            }
            user.getRoles().add(roleService.findByName("ADMIN").get()); 
            userService.save(user); 
        } else {
            requestService.submitRequest(user.getId());
        }

        return "redirect:/settings"; 
    }
}
