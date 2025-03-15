package com.skillsharing.controller;

import com.skillsharing.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);
        
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("fileDownloadUri", "/api/files/download/" + fileName);
        response.put("fileType", file.getContentType());
        response.put("size", String.valueOf(file.getSize()));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        // Load file as Resource
        byte[] fileData = fileStorageService.getFile(fileName);
        ByteArrayResource resource = new ByteArrayResource(fileData);

        // Try to determine file's content type
        String contentType = fileStorageService.getContentType(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/view/{fileName:.+}")
    public ResponseEntity<Resource> viewFile(@PathVariable String fileName) {
        // Load file as Resource
        byte[] fileData = fileStorageService.getFile(fileName);
        ByteArrayResource resource = new ByteArrayResource(fileData);

        // Try to determine file's content type
        String contentType = fileStorageService.getContentType(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @DeleteMapping("/{fileName:.+}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteFile(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "File deleted successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/{fileName:.+}")
    public ResponseEntity<Map<String, Boolean>> checkFileExists(@PathVariable String fileName) {
        boolean exists = fileStorageService.fileExists(fileName);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }
}
