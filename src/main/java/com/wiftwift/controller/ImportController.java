package com.wiftwift.controller;

import com.wiftwift.service.ImportService;
import com.wiftwift.service.MinioService;
import com.wiftwift.util.UniqueNameException;

import jakarta.servlet.annotation.MultipartConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

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
    @Autowired
    private MinioService minioService;

    @GetMapping
    public String showImportForm() {
        return "import-form";
    }

    @PostMapping
    public String handleFileUpload(@RequestParam(name = "file") MultipartFile file,
                                   @AuthenticationPrincipal UserDetails userDetails, Model model) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        
        try {
            importService.importFromFile(file, userDetails.getUsername(), fileName);
        } catch (Exception e) {
            try {
                minioService.deleteFile(fileName);
            } catch (Exception ignored) {
            }
            model.addAttribute("error", "Что ты будешь делать, если не получится импортировать из файла? Сиять. \n Причина: " + e.getMessage());
            return "error";
        }
        return "redirect:/import-attempts/my";
    }

    @PostMapping("/plain")
    public ResponseEntity<?> handlePlainTextImport(@RequestBody String data, @AuthenticationPrincipal UserDetails userDetails) {
        MultipartFile file = new InMemoryMultipartFile(
                "import.txt",
                data.getBytes(StandardCharsets.UTF_8)
        );
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            importService.importFromFile(file, userDetails.getUsername(), fileName);

        } catch (CannotAcquireLockException e) {
            return handleFailedImport(409, fileName);
        } catch(UniqueNameException e) {
            return handleFailedImport(409, fileName);
        } catch (Exception e) {
            return handleFailedImport(500, fileName);
        }
        return ResponseEntity.ok("ok");
    }

    public ResponseEntity<?> handleFailedImport(Integer code, String fileName) {
        try {
            minioService.deleteFile(fileName);
        } catch (Exception ignored) {}
        return ResponseEntity.status(code).body("");
    }

    private class InMemoryMultipartFile implements MultipartFile {
        private final String name;
        private final byte[] content;

        public InMemoryMultipartFile(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return MediaType.TEXT_PLAIN_VALUE;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @NotNull
        @Override
        public byte[] getBytes() {
            return content;
        }

        @NotNull
        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @NotNull
        @Override
        public Resource getResource() {
            return MultipartFile.super.getResource();
        }

        @Override
        public void transferTo(@NotNull Path dest) throws IOException, IllegalStateException {
            MultipartFile.super.transferTo(dest);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            Files.write(dest.toPath(), content);
        }
    }
}
