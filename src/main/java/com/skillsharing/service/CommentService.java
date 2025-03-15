package com.skillsharing.service;

import com.skillsharing.model.Comment;
import com.skillsharing.model.Post;
import com.skillsharing.model.User;
import com.skillsharing.repository.CommentRepository;
import com.skillsharing.repository.PostRepository;
import com.skillsharing.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Comment createComment(Long postId, Long userId, String content) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        
        Comment savedComment = commentRepository.save(comment);

        // Create notification for post owner if commenter is not the post owner
        if (!post.getUser().getId().equals(userId)) {
            notificationService.createNotification(
                post.getUser(),
                user,
                post,
                "COMMENT"
            );
        }

        return savedComment;
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + id));
    }

    public Comment updateComment(Long commentId, Long userId, String newContent) {
        Comment comment = getCommentById(commentId);
        
        // Verify if user can modify the comment
        if (!comment.canModify(userRepository.getReferenceById(userId))) {
            throw new IllegalArgumentException("User is not authorized to modify this comment");
        }

        comment.setContent(newContent);
        comment.markAsEdited();
        
        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getCommentById(commentId);
        
        // Verify if user can modify the comment
        if (!comment.canModify(userRepository.getReferenceById(userId))) {
            throw new IllegalArgumentException("User is not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    public Page<Comment> getPostComments(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
            
        return commentRepository.findByPostOrderByCreatedAtDesc(post, pageable);
    }

    public Page<Comment> getUserComments(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            
        return commentRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public Long getCommentsCount(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
            
        return commentRepository.countByPost(post);
    }

    public void deleteAllPostComments(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
            
        commentRepository.deleteByPost(post);
    }
}
