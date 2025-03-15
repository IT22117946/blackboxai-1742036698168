package com.skillsharing.service;

import com.skillsharing.config.FileStorageConfig;
import com.skillsharing.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;

    public String storeFile(MultipartFile file) {
        validateFile(file);

        // Generate unique filename
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = fileStorageConfig.getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            // Check if the filename contains invalid characters
            if (fileName.contains("..")) {
                throw new BadRequestException("Filename contains invalid path sequence: " + fileName);
            }

            Path targetLocation = Paths.get(fileStorageConfig.getUploadDir()).resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        String contentType = file.getContentType();
        if (!fileStorageConfig.isFileTypeAllowed(contentType)) {
            throw new BadRequestException("File type not allowed. Allowed types: " + 
                String.join(", ", fileStorageConfig.getAllowedFileTypes()));
        }

        if (!fileStorageConfig.isFileSizeAllowed(file.getSize())) {
            throw new BadRequestException("File size exceeds maximum allowed size of " + 
                fileStorageConfig.getMaxFileSize() + " bytes");
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(fileStorageConfig.getUploadDir()).resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("Error deleting file: " + fileName, ex);
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }

    public byte[] getFile(String fileName) {
        try {
            Path filePath = Paths.get(fileStorageConfig.getUploadDir()).resolve(fileName);
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            log.error("Error reading file: " + fileName, ex);
            throw new RuntimeException("Could not read file " + fileName, ex);
        }
    }

    public boolean fileExists(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        Path filePath = Paths.get(fileStorageConfig.getUploadDir()).resolve(fileName);
        return Files.exists(filePath);
    }

    public String getContentType(String fileName) {
        String extension = fileStorageConfig.getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".mp4":
                return "video/mp4";
            case ".mov":
                return "video/quicktime";
            default:
                return "application/octet-stream";
        }
    }
}
