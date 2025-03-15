package com.skillsharing.security;

import jakarta.servlet.http.Cookie;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException exception;

    @InjectMocks
    private OAuth2AuthenticationFailureHandler failureHandler;

    @BeforeEach
    void setUp() {
        when(exception.getLocalizedMessage()).thenReturn("Authentication failed");
    }

    @Test
    void onAuthenticationFailure_WithRedirectUri_ShouldRedirectWithError() throws IOException {
        // Arrange
        String redirectUri = "http://localhost:3000/oauth2/redirect";
        Cookie redirectUriCookie = new Cookie(
            HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME,
            redirectUri
        );

        when(request.getCookies()).thenReturn(new Cookie[]{redirectUriCookie});

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect(argThat(url -> 
            url.startsWith(redirectUri) && 
            url.contains("error=") && 
            url.contains("Authentication+failed")
        ));
        verify(httpCookieOAuth2AuthorizationRequestRepository)
            .removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationFailure_WithoutRedirectUri_ShouldRedirectToDefaultUri() throws IOException {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect(argThat(url -> 
            url.startsWith("/") && 
            url.contains("error=") && 
            url.contains("Authentication+failed")
        ));
        verify(httpCookieOAuth2AuthorizationRequestRepository)
            .removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationFailure_WithNullCookies_ShouldRedirectToDefaultUri() throws IOException {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect(argThat(url -> 
            url.startsWith("/") && 
            url.contains("error=") && 
            url.contains("Authentication+failed")
        ));
        verify(httpCookieOAuth2AuthorizationRequestRepository)
            .removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationFailure_WithEmptyCookies_ShouldRedirectToDefaultUri() throws IOException {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect(argThat(url -> 
            url.startsWith("/") && 
            url.contains("error=") && 
            url.contains("Authentication+failed")
        ));
        verify(httpCookieOAuth2AuthorizationRequestRepository)
            .removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationFailure_WithNullException_ShouldRedirectWithGenericError() throws IOException {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});

        // Act
        failureHandler.onAuthenticationFailure(request, response, null);

        // Assert
        verify(response).sendRedirect(argThat(url -> 
            url.startsWith("/") && 
            url.contains("error=Authentication+failed")
        ));
        verify(httpCookieOAuth2AuthorizationRequestRepository)
            .removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationFailure_WithResponseCommitted_ShouldNotRedirect() throws IOException {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});
        when(response.isCommitted()).thenReturn(true);

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response, never()).sendRedirect(any());
        verify(httpCookieOAuth2AuthorizationRequestRepository)
            .removeAuthorizationRequestCookies(request, response);
    }
}
