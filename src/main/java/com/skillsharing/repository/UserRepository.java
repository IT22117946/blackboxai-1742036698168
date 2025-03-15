package com.skillsharing.repository;

import com.skillsharing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByProviderId(String providerId);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);
    
    @Query("SELECT COUNT(f) > 0 FROM User u JOIN u.followers f WHERE u.id = :userId AND f.id = :followerId")
    boolean isFollowing(@Param("userId") Long userId, @Param("followerId") Long followerId);
    
    @Query("SELECT u FROM User u WHERE u.id IN (SELECT f.id FROM User u2 JOIN u2.followers f WHERE u2.id = :userId)")
    List<User> findFollowersByUserId(@Param("userId") Long userId);
    
    @Query("SELECT u FROM User u WHERE u.id IN (SELECT f.id FROM User u2 JOIN u2.following f WHERE u2.id = :userId)")
    List<User> findFollowingByUserId(@Param("userId") Long userId);
}
