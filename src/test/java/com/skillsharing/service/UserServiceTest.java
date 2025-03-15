package com.skillsharing.service;

import com.skillsharing.exception.ResourceNotFoundException;
import com.skillsharing.model.User;
import com.skillsharing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setProvider(User.AuthProvider.LOCAL);
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
    }

    @Test
    void getUserById_NonExistingUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(999L);
        });
    }

    @Test
    void getUserByEmail_ExistingUser_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByEmail(testUser.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void getUserByEmail_NonExistingUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.getUserByEmail("nonexistent@example.com");
        });
    }

    @Test
    void createUser_NewUser_ShouldCreateSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ExistingEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(testUser);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ExistingUser_ShouldUpdateSuccessfully() {
        // Arrange
        User updatedDetails = new User();
        updatedDetails.setName("Updated Name");
        updatedDetails.setBio("Updated Bio");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(1L, updatedDetails);

        // Assert
        assertEquals(updatedDetails.getName(), result.getName());
        assertEquals(updatedDetails.getBio(), result.getBio());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void followUser_ValidUsers_ShouldFollowSuccessfully() {
        // Arrange
        User follower = testUser;
        User following = new User();
        following.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(userRepository.isFollowing(2L, 1L)).thenReturn(false);

        // Act
        userService.followUser(1L, 2L);

        // Assert
        verify(userRepository).save(follower);
        verify(userRepository).save(following);
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(testUser);
        when(userRepository.searchUsers("test")).thenReturn(expectedUsers);

        // Act
        List<User> result = userService.searchUsers("test");

        // Assert
        assertEquals(expectedUsers.size(), result.size());
        assertEquals(expectedUsers.get(0).getName(), result.get(0).getName());
    }

    @Test
    void isFollowing_ShouldReturnCorrectStatus() {
        // Arrange
        when(userRepository.isFollowing(2L, 1L)).thenReturn(true);

        // Act
        boolean result = userService.isFollowing(2L, 1L);

        // Assert
        assertTrue(result);
    }
}
