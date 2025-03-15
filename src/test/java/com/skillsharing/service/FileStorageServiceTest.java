package com.skillsharing.service;

import com.skillsharing.config.FileStorageConfig;
import com.skillsharing.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private FileStorageConfig fileStorageConfig;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageConfig = new FileStorageConfig();
        fileStorageConfig.setUploadDir(tempDir.toString());
        fileStorageConfig.setMaxFileSize(5242880L); // 5MB
        fileStorageConfig.setAllowedFileTypes(new String[]{
            "image/jpeg",
            "image/png",
            "video/mp4"
        });
        fileStorageConfig.setMaxVideoLength(30);

        fileStorageService = new FileStorageService(fileStorageConfig);
    }

    @Test
    void storeFile_ValidImage_ShouldStoreSuccessfully() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        // Act
        String fileName = fileStorageService.storeFile(file);

        // Assert
        assertTrue(Files.exists(tempDir.resolve(fileName)));
        assertTrue(fileName.endsWith(".jpg"));
    }

    @Test
    void storeFile_EmptyFile_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.jpg",
            "image/jpeg",
            new byte[0]
        );

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    void storeFile_InvalidFileType_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    void storeFile_ExceedingSizeLimit_ShouldThrowException() {
        // Arrange
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "large-image.jpg",
            "image/jpeg",
            largeContent
        );

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    void getFile_ExistingFile_ShouldReturnContent() throws IOException {
        // Arrange
        String content = "test content";
        Path filePath = tempDir.resolve("test-file.txt");
        Files.write(filePath, content.getBytes());

        // Act
        byte[] retrievedContent = fileStorageService.getFile("test-file.txt");

        // Assert
        assertArrayEquals(content.getBytes(), retrievedContent);
    }

    @Test
    void getFile_NonExistentFile_ShouldThrowException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            fileStorageService.getFile("non-existent.txt");
        });
    }

    @Test
    void deleteFile_ExistingFile_ShouldDeleteSuccessfully() throws IOException {
        // Arrange
        Path filePath = tempDir.resolve("to-delete.txt");
        Files.write(filePath, "content".getBytes());

        // Act
        fileStorageService.deleteFile("to-delete.txt");

        // Assert
        assertFalse(Files.exists(filePath));
    }

    @Test
    void fileExists_ShouldReturnCorrectStatus() throws IOException {
        // Arrange
        Path filePath = tempDir.resolve("existing-file.txt");
        Files.write(filePath, "content".getBytes());

        // Act & Assert
        assertTrue(fileStorageService.fileExists("existing-file.txt"));
        assertFalse(fileStorageService.fileExists("non-existing-file.txt"));
    }

    @Test
    void getContentType_ShouldReturnCorrectType() {
        // Act & Assert
        assertEquals("image/jpeg", fileStorageService.getContentType("test.jpg"));
        assertEquals("image/png", fileStorageService.getContentType("test.png"));
        assertEquals("video/mp4", fileStorageService.getContentType("test.mp4"));
        assertEquals("application/octet-stream", fileStorageService.getContentType("test.unknown"));
    }
}
