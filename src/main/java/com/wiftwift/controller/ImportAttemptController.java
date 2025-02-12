package com.wiftwift.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.wiftwift.entity.ImportAttempt;
import com.wiftwift.entity.User;
import com.wiftwift.service.ImportAttemptService;
import com.wiftwift.service.UserService;

@Controller
@RequestMapping("/import-attempts")
public class ImportAttemptController {

    @Autowired
    private ImportAttemptService importAttemptService;

    @Autowired
    private UserService userService;

    @GetMapping("/my")
    public String getUserAttempts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Sort sort = Sort.by(sortDirection.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ImportAttempt> attemptsPage = importAttemptService.getUserAttempts(user.getId(), pageable);

        model.addAttribute("attemptsPage", attemptsPage);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("currentPath", "/import-attempts/my");
        model.addAttribute("currentUsername", userDetails.getUsername());
        model.addAttribute("isAdmin", false);

        return "import-attempts";
    }

    @GetMapping("/all")
    public String getAllAttempts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return "redirect:/import-attempts/my";
        }

        Sort sort = Sort.by(sortDirection.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ImportAttempt> attemptsPage = importAttemptService.getAllAttempts(pageable);

        model.addAttribute("attemptsPage", attemptsPage);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("currentPath", "/import-attempts/all");
        model.addAttribute("currentUsername", userDetails.getUsername());
        model.addAttribute("isAdmin", true);

        return "import-attempts";
    }
}