package com.skillsharing.repository;

import com.skillsharing.model.Comment;
import com.skillsharing.model.Post;
import com.skillsharing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Find comments by post
    Page<Comment> findByPostOrderByCreatedAtDesc(Post post, Pageable pageable);
    
    // Find comments by user
    Page<Comment> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Count comments for a post
    Long countByPost(Post post);
    
    // Delete all comments for a post
    void deleteByPost(Post post);
}
