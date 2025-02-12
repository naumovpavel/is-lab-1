package com.wiftwift.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.annotation.MultipartConfig;
import com.wiftwift.service.ImportService;

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

        System.out.println("handleFileUpload");
        try {
            importService.processImport(file, userDetails.getUsername(), 3);
        } catch (Exception e) {
            System.err.println("Error processing import: " + e.getMessage());
            model.addAttribute("error", "Что ты будешь делать, если не получится импортировать из файла? Сиять. \n Причина: " + e.getMessage());
            return "error";
        }
        return "redirect:/import-attempts/my";
    }
}
