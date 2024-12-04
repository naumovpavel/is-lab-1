package com.wiftwift.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wiftwift.dto.RequestDto;
import com.wiftwift.entity.Request;
import com.wiftwift.service.RequestService;
import com.wiftwift.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String redirectToRequests() {
        return "redirect:/admin/requests";
    }

    @GetMapping("/requests")
    public String showRequests(Model model, Authentication authentication) {
        boolean isAdmin = userService.isAdmin(authentication.getName());
        if (!isAdmin) {
            return "redirect:/settings"; 
        }

        List<Request> requests = requestService.getAllRequests();
        model.addAttribute("requests", requests);
        model.addAttribute("username", authentication.getName());

        return "admin-requests"; 
    }

    @Transactional
    @PostMapping("/requests/accept")
    public String acceptRequest(@ModelAttribute RequestDto requestDto) {
        Long requestId = requestDto.getRequestId();
        requestService.acceptRequest(requestId);
        return "redirect:/admin/requests"; 
    }

    @Transactional
    @PostMapping("/requests/reject")
    public String rejectRequest(@ModelAttribute RequestDto requestDto) {
        Long requestId = requestDto.getRequestId();
        requestService.rejectRequest(requestId);
        return "redirect:/admin/requests"; 
    }
}
