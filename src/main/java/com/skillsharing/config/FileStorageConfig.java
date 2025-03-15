package com.skillsharing.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "file")
@Data
public class FileStorageConfig {
    
    private String uploadDir;
    private long maxFileSize = 10485760; // 10MB default
    private String[] allowedFileTypes = {
        "image/jpeg",
        "image/png",
        "image/gif",
        "video/mp4",
        "video/quicktime"
    };
    private int maxVideoLength = 30; // 30 seconds

    @PostConstruct
    public void init() {
        try {
            if (!StringUtils.hasText(uploadDir)) {
                uploadDir = System.getProperty("user.home") + "/uploads/skillsharing";
            }
            
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    public boolean isFileTypeAllowed(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        for (String allowedType : allowedFileTypes) {
            if (contentType.equalsIgnoreCase(allowedType)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFileSizeAllowed(long size) {
        return size <= maxFileSize;
    }

    public String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        String extension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = fileName.substring(lastDotIndex);
        }
        return extension.toLowerCase();
    }

    public boolean isVideoFile(String contentType) {
        return contentType != null && contentType.startsWith("video/");
    }

    public boolean isImageFile(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
}
