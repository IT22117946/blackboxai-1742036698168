package com.skillsharing.security;

import com.skillsharing.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    private User user;
    private UserPrincipal userPrincipal;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setName("Test User");
        user.setProvider(User.AuthProvider.LOCAL);

        userPrincipal = UserPrincipal.create(user);

        attributes = new HashMap<>();
        attributes.put("sub", "123456");
        attributes.put("name", "Test OAuth User");
        attributes.put("email", "oauth@example.com");
    }

    @Test
    void create_FromUser_ShouldCreateUserPrincipal() {
        // Act
        UserPrincipal result = UserPrincipal.create(user);

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getPassword(), result.getPassword());
        assertEquals(user.getName(), result.getName());
    }

    @Test
    void create_FromUserAndAttributes_ShouldCreateOAuth2UserPrincipal() {
        // Act
        OAuth2User result = UserPrincipal.create(user, attributes);

        // Assert
        assertNotNull(result);
        assertEquals(attributes, result.getAttributes());
        assertEquals(user.getId(), result.getAttribute("id"));
        assertEquals(user.getName(), result.getAttribute("name"));
        assertEquals(user.getEmail(), result.getAttribute("email"));
    }

    @Test
    void getAuthorities_ShouldReturnUserRole() {
        // Act
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void getPassword_ShouldReturnUserPassword() {
        // Act & Assert
        assertEquals(user.getPassword(), userPrincipal.getPassword());
    }

    @Test
    void getUsername_ShouldReturnUserEmail() {
        // Act & Assert
        assertEquals(user.getEmail(), userPrincipal.getUsername());
    }

    @Test
    void isAccountNonExpired_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(userPrincipal.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(userPrincipal.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(userPrincipal.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(userPrincipal.isEnabled());
    }

    @Test
    void getAttributes_WithNoAttributes_ShouldReturnNull() {
        // Act & Assert
        assertNull(userPrincipal.getAttributes());
    }

    @Test
    void getAttributes_WithAttributes_ShouldReturnAttributes() {
        // Arrange
        UserPrincipal principalWithAttributes = UserPrincipal.create(user, attributes);

        // Act & Assert
        assertEquals(attributes, principalWithAttributes.getAttributes());
    }

    @Test
    void getName_ShouldReturnUserId() {
        // Act & Assert
        assertEquals(user.getId().toString(), userPrincipal.getName());
    }

    @Test
    void equals_WithSameUser_ShouldReturnTrue() {
        // Arrange
        UserPrincipal samePrincipal = UserPrincipal.create(user);

        // Act & Assert
        assertEquals(userPrincipal, samePrincipal);
        assertEquals(userPrincipal.hashCode(), samePrincipal.hashCode());
    }

    @Test
    void equals_WithDifferentUser_ShouldReturnFalse() {
        // Arrange
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setEmail("different@example.com");
        UserPrincipal differentPrincipal = UserPrincipal.create(differentUser);

        // Act & Assert
        assertNotEquals(userPrincipal, differentPrincipal);
        assertNotEquals(userPrincipal.hashCode(), differentPrincipal.hashCode());
    }
}
