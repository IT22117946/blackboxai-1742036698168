package com.skillsharing.security;

import com.skillsharing.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenProviderTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Auth auth;

    private TokenProvider tokenProvider;
    private UserPrincipal userPrincipal;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        when(appProperties.getAuth()).thenReturn(auth);
        when(auth.getTokenExpirationMsec()).thenReturn(864000000L); // 10 days

        tokenProvider = new TokenProvider(appProperties);

        userPrincipal = new UserPrincipal(
            1L,
            "test@example.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            userPrincipal.getAuthorities()
        );
    }

    @Test
    void createToken_ValidAuthentication_ShouldCreateToken() {
        // Act
        String token = tokenProvider.createToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void getUserIdFromToken_ValidToken_ShouldReturnUserId() {
        // Arrange
        String token = tokenProvider.createToken(authentication);

        // Act
        Long userId = tokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(userPrincipal.getId(), userId);
    }

    @Test
    void validateToken_ValidToken_ShouldReturnTrue() {
        // Arrange
        String token = tokenProvider.createToken(authentication);

        // Act
        boolean isValid = tokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.string";

        // Act
        boolean isValid = tokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_ExpiredToken_ShouldReturnFalse() {
        // Arrange
        when(auth.getTokenExpirationMsec()).thenReturn(0L); // Immediate expiration
        TokenProvider shortLivedTokenProvider = new TokenProvider(appProperties);
        String token = shortLivedTokenProvider.createToken(authentication);

        // Act
        boolean isValid = shortLivedTokenProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_NullToken_ShouldReturnFalse() {
        // Act
        boolean isValid = tokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_EmptyToken_ShouldReturnFalse() {
        // Act
        boolean isValid = tokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void createAndValidateToken_ShouldWorkTogether() {
        // Arrange & Act
        String token = tokenProvider.createToken(authentication);
        boolean isValid = tokenProvider.validateToken(token);
        Long userId = tokenProvider.getUserIdFromToken(token);

        // Assert
        assertTrue(isValid);
        assertEquals(userPrincipal.getId(), userId);
    }
}
