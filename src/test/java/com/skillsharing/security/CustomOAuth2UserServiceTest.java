package com.skillsharing.security;

import com.skillsharing.exception.OAuth2AuthenticationProcessingException;
import com.skillsharing.model.User;
import com.skillsharing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomOAuth2UserService oAuth2UserService;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private ClientRegistration clientRegistration;

    @Mock
    private OAuth2AccessToken accessToken;

    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
        attributes.put("sub", "123456");
        attributes.put("name", "Test User");
        attributes.put("email", "test@example.com");
        attributes.put("picture", "https://example.com/photo.jpg");

        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(oAuth2UserRequest.getAccessToken()).thenReturn(accessToken);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        when(accessToken.getTokenValue()).thenReturn("token");
        when(accessToken.getExpiresAt()).thenReturn(Instant.now().plusSeconds(3600));
    }

    @Test
    void loadUser_NewGoogleUser_ShouldRegisterAndReturnUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        OAuth2User result = oAuth2UserService.loadUser(oAuth2UserRequest);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getAttributes().get("email"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loadUser_ExistingGoogleUser_ShouldUpdateAndReturnUser() {
        // Arrange
        User existingUser = new User();
        existingUser.setEmail("test@example.com");
        existingUser.setProvider(User.AuthProvider.GOOGLE);
        existingUser.setProviderId("123456");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        OAuth2User result = oAuth2UserService.loadUser(oAuth2UserRequest);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getAttributes().get("email"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loadUser_UserWithDifferentProvider_ShouldThrowException() {
        // Arrange
        User existingUser = new User();
        existingUser.setEmail("test@example.com");
        existingUser.setProvider(User.AuthProvider.FACEBOOK);
        existingUser.setProviderId("different-id");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(OAuth2AuthenticationException.class, () -> {
            oAuth2UserService.loadUser(oAuth2UserRequest);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loadUser_MissingEmail_ShouldThrowException() {
        // Arrange
        attributes.remove("email");

        // Act & Assert
        assertThrows(OAuth2AuthenticationException.class, () -> {
            oAuth2UserService.loadUser(oAuth2UserRequest);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loadUser_UnsupportedProvider_ShouldThrowException() {
        // Arrange
        when(clientRegistration.getRegistrationId()).thenReturn("unsupported");

        // Act & Assert
        assertThrows(OAuth2AuthenticationProcessingException.class, () -> {
            oAuth2UserService.loadUser(oAuth2UserRequest);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loadUser_NullAttributes_ShouldThrowException() {
        // Arrange
        when(oAuth2User.getAttributes()).thenReturn(null);

        // Act & Assert
        assertThrows(OAuth2AuthenticationException.class, () -> {
            oAuth2UserService.loadUser(oAuth2UserRequest);
        });
        verify(userRepository, never()).save(any(User.class));
    }
}
