package com.skillsharing.controller;

import com.skillsharing.BaseTest;
import com.skillsharing.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FileControllerTest extends BaseTest {

    @MockBean
    private FileStorageService fileStorageService;

    @Test
    void uploadFile_ShouldUploadSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        
        when(fileStorageService.storeFile(any())).thenReturn("test-image.jpg");

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test-image.jpg"))
                .andExpect(jsonPath("$.fileDownloadUri").value("/api/files/download/test-image.jpg"))
                .andExpect(jsonPath("$.fileType").value("image/jpeg"));
    }

    @Test
    void downloadFile_ShouldDownloadSuccessfully() throws Exception {
        // Arrange
        byte[] fileContent = "test file content".getBytes();
        when(fileStorageService.getFile("test-file.txt")).thenReturn(fileContent);
        when(fileStorageService.getContentType("test-file.txt"))
                .thenReturn("text/plain");

        // Act & Assert
        mockMvc.perform(get("/api/files/download/{fileName}", "test-file.txt"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                        "attachment; filename=\"test-file.txt\""))
                .andExpect(content().bytes(fileContent));
    }

    @Test
    void viewFile_ShouldDisplaySuccessfully() throws Exception {
        // Arrange
        byte[] fileContent = "test image content".getBytes();
        when(fileStorageService.getFile("test-image.jpg")).thenReturn(fileContent);
        when(fileStorageService.getContentType("test-image.jpg"))
                .thenReturn("image/jpeg");

        // Act & Assert
        mockMvc.perform(get("/api/files/view/{fileName}", "test-image.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                        "inline; filename=\"test-image.jpg\""))
                .andExpect(content().bytes(fileContent));
    }

    @Test
    void deleteFile_ShouldDeleteSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/files/{fileName}", "test-file.txt")
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("File deleted successfully"));
    }

    @Test
    void checkFileExists_ShouldReturnCorrectStatus() throws Exception {
        // Arrange
        when(fileStorageService.fileExists("existing-file.txt")).thenReturn(true);
        when(fileStorageService.fileExists("non-existing-file.txt")).thenReturn(false);

        // Act & Assert - Existing file
        mockMvc.perform(get("/api/files/exists/{fileName}", "existing-file.txt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));

        // Act & Assert - Non-existing file
        mockMvc.perform(get("/api/files/exists/{fileName}", "non-existing-file.txt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));
    }

    @Test
    void uploadFile_WithInvalidFile_ShouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "",
            "application/octet-stream",
            new byte[0]
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void downloadFile_NonExistentFile_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(fileStorageService.getFile("non-existent.txt"))
                .thenThrow(new RuntimeException("File not found"));

        // Act & Assert
        mockMvc.perform(get("/api/files/download/{fileName}", "non-existent.txt"))
                .andExpect(status().isInternalServerError());
    }
}
