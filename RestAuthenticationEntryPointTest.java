package com.skillsharing.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    private StringWriter stringWriter;

    @BeforeEach
    void setUp() {
        stringWriter = new StringWriter();
        when(authException.getMessage()).thenReturn("Unauthorized access");
    }

    @Test
    void commence_ShouldSendUnauthorizedError() throws IOException, ServletException {
        // Arrange
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // Act
        authenticationEntryPoint.commence(request, response, authException);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String expectedJson = "{ \"error\": \"Unauthorized access\", " +
                            "\"message\": \"You need to login to access this resource\" }";
        // Remove whitespace for comparison
        String actualJson = stringWriter.toString().replaceAll("\\s+", "");
        String expectedJsonNoSpace = expectedJson.replaceAll("\\s+", "");
        assert(actualJson.equals(expectedJsonNoSpace));
    }

    @Test
    void commence_WithIOException_ShouldHandleGracefully() throws IOException, ServletException {
        // Arrange
        when(response.getWriter()).thenThrow(new IOException("Error writing response"));

        // Act
        authenticationEntryPoint.commence(request, response, authException);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // No exception should be thrown
    }

    @Test
    void commence_WithNullAuthException_ShouldSendGenericMessage() throws IOException, ServletException {
        // Arrange
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // Act
        authenticationEntryPoint.commence(request, response, null);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String expectedJson = "{ \"error\": \"Unauthorized\", " +
                            "\"message\": \"You need to login to access this resource\" }";
        // Remove whitespace for comparison
        String actualJson = stringWriter.toString().replaceAll("\\s+", "");
        String expectedJsonNoSpace = expectedJson.replaceAll("\\s+", "");
        assert(actualJson.equals(expectedJsonNoSpace));
    }

    @Test
    void commence_WithEmptyMessage_ShouldSendGenericMessage() throws IOException, ServletException {
        // Arrange
        when(authException.getMessage()).thenReturn("");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // Act
        authenticationEntryPoint.commence(request, response, authException);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String expectedJson = "{ \"error\": \"Unauthorized\", " +
                            "\"message\": \"You need to login to access this resource\" }";
        // Remove whitespace for comparison
        String actualJson = stringWriter.toString().replaceAll("\\s+", "");
        String expectedJsonNoSpace = expectedJson.replaceAll("\\s+", "");
        assert(actualJson.equals(expectedJsonNoSpace));
    }

    @Test
    void commence_WithResponseCommitted_ShouldNotWrite() throws IOException, ServletException {
        // Arrange
        when(response.isCommitted()).thenReturn(true);

        // Act
        authenticationEntryPoint.commence(request, response, authException);

        // Assert
        verify(response, never()).getWriter();
        verify(response, never()).setContentType(anyString());
        verify(response, never()).setStatus(anyInt());
    }
}
