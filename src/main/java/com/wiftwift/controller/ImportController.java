package com.wiftwift.controller;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;

import com.wiftwift.entity.Chapter;
import com.wiftwift.entity.User;
import com.wiftwift.service.ImportService;
import com.wiftwift.util.UniqueNameException;

@Controller
@RequestMapping("/import")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 10,  // 10 MB
    maxRequestSize = 1024 * 1024 * 15 // 15 MB
)
public class ImportController {

    @Autowired
    private ImportService importService;

    @GetMapping
    public String showImportForm() {
        return "import-form";
    }

    @PostMapping
    public String handleFileUpload(@RequestParam(name="file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails, Model model) {

        try {
            importService.processImport(file.getInputStream(), userDetails.getUsername(), 3);
        } catch (Exception e) {
            model.addAttribute("error", "Что ты будешь делать, если не получится импортировать из файла? Сиять. \n Причина: " + e.getMessage());
            return "error";
        }
        return "redirect:/import-attempts/my";
    }

    @PostMapping("/plain")
    public ResponseEntity<String> handleFileUpload(@Valid @RequestBody String data, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            importService.processImport(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), userDetails.getUsername(), 3);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("");
        }
        return ResponseEntity.ok("ok");
    }
}
