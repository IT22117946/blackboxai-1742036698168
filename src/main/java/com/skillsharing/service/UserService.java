package com.skillsharing.service;

import com.skillsharing.model.User;
import com.skillsharing.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        
        user.setName(userDetails.getName());
        user.setBio(userDetails.getBio());
        if (userDetails.getProfilePicture() != null) {
            user.setProfilePicture(userDetails.getProfilePicture());
        }
        
        return userRepository.save(user);
    }

    public void followUser(Long userId, Long followId) {
        if (userId.equals(followId)) {
            throw new IllegalArgumentException("Users cannot follow themselves");
        }

        User user = getUserById(userId);
        User followUser = getUserById(followId);

        if (userRepository.isFollowing(followId, userId)) {
            throw new IllegalArgumentException("Already following this user");
        }

        user.getFollowing().add(followUser);
        followUser.getFollowers().add(user);
        
        userRepository.save(user);
        userRepository.save(followUser);
    }

    public void unfollowUser(Long userId, Long unfollowId) {
        User user = getUserById(userId);
        User unfollowUser = getUserById(unfollowId);

        if (!userRepository.isFollowing(unfollowId, userId)) {
            throw new IllegalArgumentException("Not following this user");
        }

        user.getFollowing().remove(unfollowUser);
        unfollowUser.getFollowers().remove(user);
        
        userRepository.save(user);
        userRepository.save(unfollowUser);
    }

    public List<User> getFollowers(Long userId) {
        return userRepository.findFollowersByUserId(userId);
    }

    public List<User> getFollowing(Long userId) {
        return userRepository.findFollowingByUserId(userId);
    }

    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }

    public boolean isFollowing(Long userId, Long followerId) {
        return userRepository.isFollowing(userId, followerId);
    }
}
