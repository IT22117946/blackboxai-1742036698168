package com.skillsharing.controller;

import com.skillsharing.model.User;
import com.skillsharing.security.CurrentUser;
import com.skillsharing.security.UserPrincipal;
import com.skillsharing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<User> getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        User user = userService.getUserById(userPrincipal.getId());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserProfile(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<User> updateCurrentUser(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(userPrincipal.getId(), userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{id}/follow")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> followUser(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id) {
        userService.followUser(userPrincipal.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unfollow")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> unfollowUser(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id) {
        userService.unfollowUser(userPrincipal.getId(), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<List<User>> getUserFollowers(@PathVariable Long id) {
        List<User> followers = userService.getFollowers(id);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<List<User>> getUserFollowing(@PathVariable Long id) {
        List<User> following = userService.getFollowing(id);
        return ResponseEntity.ok(following);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String keyword) {
        List<User> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/is-following")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> checkIfFollowing(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id) {
        boolean isFollowing = userService.isFollowing(id, userPrincipal.getId());
        return ResponseEntity.ok(isFollowing);
    }
}
