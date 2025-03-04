package com.wiftwift.controller;

import com.wiftwift.entity.ImportAttempt;
import com.wiftwift.entity.User;
import com.wiftwift.service.ImportAttemptService;
import com.wiftwift.service.MinioService;
import com.wiftwift.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/import-attempts")
public class ImportAttemptController {

    @Autowired
    private ImportAttemptService importAttemptService;

    @Autowired
    private UserService userService;

    @Autowired
    private MinioService minioService;

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

    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable("id") Long id) {
        // Получаем попытку импорта
        ImportAttempt attempt = importAttemptService.getById(id).orElseThrow(
                () -> new ResponseStatusException(NOT_FOUND, "Import attempt not found")
        );

        // Проверяем статус accepted
        if (!attempt.isAccepted()) {
            throw new ResponseStatusException(NOT_FOUND, "Attempt not accepted");
        }

        // Получаем файл из Minio
        try {
            InputStream fileStream = minioService.getFile(attempt.getFilename());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .header("Content-Disposition", "attachment; filename=\"" + attempt.getFilename() + "\"")
                    .body(new InputStreamResource(fileStream));
        } catch (Exception e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Error retrieving file", e);
        }
    }
}