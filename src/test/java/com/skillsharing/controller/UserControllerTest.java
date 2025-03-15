package com.skillsharing.controller;

import com.skillsharing.BaseTest;
import com.skillsharing.model.User;
import com.skillsharing.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest extends BaseTest {

    @MockBean
    private UserService userService;

    @Test
    void getCurrentUser_ShouldReturnCurrentUser() throws Exception {
        // Arrange
        when(userService.getUserById(testUser.getId())).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    void getUserProfile_ShouldReturnUserProfile() throws Exception {
        // Arrange
        when(userService.getUserById(testUser.getId())).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value(testUser.getName()));
    }

    @Test
    void updateCurrentUser_ShouldUpdateAndReturnUser() throws Exception {
        // Arrange
        User updatedUser = createTestUser();
        updatedUser.setName("Updated Name");
        updatedUser.setBio("Updated Bio");

        when(userService.updateUser(eq(testUser.getId()), any(User.class)))
                .thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.bio").value("Updated Bio"));
    }

    @Test
    void followUser_ShouldFollowSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/{id}/follow", 2L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk());
    }

    @Test
    void unfollowUser_ShouldUnfollowSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/{id}/unfollow", 2L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk());
    }

    @Test
    void getUserFollowers_ShouldReturnFollowers() throws Exception {
        // Arrange
        List<User> followers = Arrays.asList(
            createTestUser(),
            createTestUser()
        );
        when(userService.getFollowers(testUser.getId())).thenReturn(followers);

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}/followers", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUserFollowing_ShouldReturnFollowing() throws Exception {
        // Arrange
        List<User> following = Arrays.asList(
            createTestUser(),
            createTestUser()
        );
        when(userService.getFollowing(testUser.getId())).thenReturn(following);

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}/following", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(
            createTestUser(),
            createTestUser()
        );
        when(userService.searchUsers("test")).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/users/search")
                .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void checkIfFollowing_ShouldReturnCorrectStatus() throws Exception {
        // Arrange
        when(userService.isFollowing(2L, testUser.getId())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}/is-following", 2L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
