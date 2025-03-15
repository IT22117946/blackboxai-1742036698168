package com.skillsharing.repository;

import com.skillsharing.model.Post;
import com.skillsharing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Find posts by user
    Page<Post> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find posts by type
    Page<Post> findByPostTypeOrderByCreatedAtDesc(Post.PostType postType, Pageable pageable);
    
    // Find posts from users that the current user follows
    @Query("SELECT p FROM Post p WHERE p.user.id IN " +
           "(SELECT f.id FROM User u JOIN u.following f WHERE u.id = :userId) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findFollowingUsersPosts(@Param("userId") Long userId, Pageable pageable);
    
    // Search posts by description or user name
    @Query("SELECT p FROM Post p WHERE " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> searchPosts(@Param("keyword") String keyword, Pageable pageable);
    
    // Find learning plans that are ongoing or upcoming
    @Query("SELECT p FROM Post p WHERE p.postType = 'LEARNING_PLAN' AND " +
           "p.planEndDate >= CURRENT_DATE ORDER BY p.planStartDate ASC")
    List<Post> findActiveLearningPlans();
    
    // Find posts with most likes
    @Query("SELECT p FROM Post p LEFT JOIN p.likes l " +
           "GROUP BY p ORDER BY COUNT(l) DESC")
    Page<Post> findMostLikedPosts(Pageable pageable);
    
    // Find posts with most comments
    @Query("SELECT p FROM Post p LEFT JOIN p.comments c " +
           "GROUP BY p ORDER BY COUNT(c) DESC")
    Page<Post> findMostCommentedPosts(Pageable pageable);
    
    // Count likes for a post
    @Query("SELECT COUNT(l) FROM Post p JOIN p.likes l WHERE p.id = :postId")
    Long countLikesByPostId(@Param("postId") Long postId);
    
    // Check if a user has liked a post
    @Query("SELECT COUNT(l) > 0 FROM Post p JOIN p.likes l " +
           "WHERE p.id = :postId AND l.id = :userId")
    boolean hasUserLikedPost(@Param("postId") Long postId, @Param("userId") Long userId);
}
