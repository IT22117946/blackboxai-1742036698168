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
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private HttpCookieOAuth2AuthorizationRequestRepository repository;

    private OAuth2AuthorizationRequest authorizationRequest;
    private Cookie authRequestCookie;
    private Cookie redirectUriCookie;

    @BeforeEach
    void setUp() {
        authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
            .clientId("client-id")
            .authorizationUri("http://auth-server.com/oauth/authorize")
            .redirectUri("http://client.com/callback")
            .state("state")
            .build();

        String serializedRequest = CookieUtils.serialize(authorizationRequest);
        authRequestCookie = new Cookie(
            HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
            serializedRequest
        );
        redirectUriCookie = new Cookie(
            HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME,
            "http://client.com/callback"
        );
    }

    @Test
    void loadAuthorizationRequest_WithValidCookie_ShouldReturnRequest() {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{authRequestCookie});

        // Act
        OAuth2AuthorizationRequest result = repository.loadAuthorizationRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals(authorizationRequest.getClientId(), result.getClientId());
        assertEquals(authorizationRequest.getAuthorizationUri(), result.getAuthorizationUri());
        assertEquals(authorizationRequest.getRedirectUri(), result.getRedirectUri());
    }

    @Test
    void loadAuthorizationRequest_WithNoCookie_ShouldReturnNull() {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});

        // Act
        OAuth2AuthorizationRequest result = repository.loadAuthorizationRequest(request);

        // Assert
        assertNull(result);
    }

    @Test
    void saveAuthorizationRequest_WithValidRequest_ShouldSaveCookies() {
        // Act
        repository.saveAuthorizationRequest(authorizationRequest, request, response);

        // Assert
        verify(response, times(2)).addCookie(any(Cookie.class));
    }

    @Test
    void saveAuthorizationRequest_WithNullRequest_ShouldRemoveCookies() {
        // Act
        repository.saveAuthorizationRequest(null, request, response);

        // Assert
        verify(response, times(2)).addCookie(argThat(cookie -> 
            cookie.getMaxAge() == 0
        ));
    }

    @Test
    void removeAuthorizationRequest_ShouldReturnAndRemoveRequest() {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{authRequestCookie});

        // Act
        OAuth2AuthorizationRequest result = repository.removeAuthorizationRequest(request, response);

        // Assert
        assertNotNull(result);
        assertEquals(authorizationRequest.getClientId(), result.getClientId());
        verify(response, times(2)).addCookie(argThat(cookie -> 
            cookie.getMaxAge() == 0
        ));
    }

    @Test
    void removeAuthorizationRequestCookies_ShouldRemoveBothCookies() {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{authRequestCookie, redirectUriCookie});

        // Act
        repository.removeAuthorizationRequestCookies(request, response);

        // Assert
        verify(response, times(2)).addCookie(argThat(cookie -> 
            cookie.getMaxAge() == 0 &&
            (cookie.getName().equals(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME) ||
             cookie.getName().equals(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME))
        ));
    }

    @Test
    void getRedirectUri_WithValidCookie_ShouldReturnUri() {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{redirectUriCookie});

        // Act
        String result = repository.getRedirectUri(request);

        // Assert
        assertEquals("http://client.com/callback", result);
    }

    @Test
    void getRedirectUri_WithNoCookie_ShouldReturnNull() {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});

        // Act
        String result = repository.getRedirectUri(request);

        // Assert
        assertNull(result);
    }
}
