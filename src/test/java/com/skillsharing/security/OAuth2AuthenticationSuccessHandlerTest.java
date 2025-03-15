package com.skillsharing.security;

import com.skillsharing.config.AppProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private AppProperties appProperties;

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler authenticationSuccessHandler;

    private UserPrincipal userPrincipal;
    private String token;
    private AppProperties.OAuth2 oauth2Properties;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(
            1L,
            "test@example.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        token = "valid.jwt.token";
        oauth2Properties = new AppProperties.OAuth2();
        oauth2Properties.setAuthorizedRedirectUris(Collections.singletonList("http://localhost:3000/oauth2/redirect"));

        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(tokenProvider.createToken(authentication)).thenReturn(token);
        when(appProperties.getOauth2()).thenReturn(oauth2Properties);
    }

    @Test
    void onAuthenticationSuccess_WithValidRedirectUri_ShouldRedirectWithToken() throws IOException {
        // Arrange
        String redirectUri = "http://localhost:3000/oauth2/redirect";
        Cookie redirectUriCookie = new Cookie(
            HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME,
            redirectUri
        );

        when(request.getCookies()).thenReturn(new Cookie[]{redirectUriCookie});
        when(response.isCommitted()).thenReturn(false);

        // Act
        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect(argThat(url -> 
            url.startsWith(redirectUri) && url.contains("token=" + token)
        ));
        verify(httpCookieOAuth2AuthorizationRequestRepository)
            .removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationSuccess_WithInvalidRedirectUri_ShouldThrowException() {
        // Arrange
        String invalidRedirectUri = "http://malicious-site.com/callback";
        Cookie redirectUriCookie = new Cookie(
            HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME,
            invalidRedirectUri
        );

        when(request.getCookies()).thenReturn(new Cookie[]{redirectUriCookie});

        // Act & Assert
        try {
            authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
        } catch (Exception e) {
            verify(response, never()).sendRedirect(any());
            verify(httpCookieOAuth2AuthorizationRequestRepository)
                .removeAuthorizationRequestCookies(request, response);
        }
    }

    @Test
    void onAuthenticationSuccess_WithNoRedirectUri_ShouldUseDefaultUri() throws IOException {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});
        when(response.isCommitted()).thenReturn(false);

        // Act
        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect(argThat(url -> 
            url.contains("token=" + token)
        ));
        verify(httpCookieOAuth2AuthorizationRequestRepository)
            .removeAuthorizationRequestCookies(request, response);
    }

    @Test
    void onAuthenticationSuccess_WithResponseCommitted_ShouldNotRedirect() throws IOException {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});
        when(response.isCommitted()).thenReturn(true);

        // Act
        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response, never()).sendRedirect(any());
    }
}
