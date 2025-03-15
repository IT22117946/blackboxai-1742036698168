package com.skillsharing.controller;

import com.skillsharing.model.Comment;
import com.skillsharing.security.CurrentUser;
import com.skillsharing.security.UserPrincipal;
import com.skillsharing.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Comment> createComment(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long postId,
            @Valid @RequestBody String content) {
        Comment comment = commentService.createComment(postId, currentUser.getId(), content);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<Comment>> getPostComments(
            @PathVariable Long postId,
            Pageable pageable) {
        Page<Comment> comments = commentService.getPostComments(postId, pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<Page<Comment>> getUserComments(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<Comment> comments = commentService.getUserComments(userId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/comments/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Comment> updateComment(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody String content) {
        Comment updatedComment = commentService.updateComment(id, currentUser.getId(), content);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteComment(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        commentService.deleteComment(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/posts/{postId}/comments/count")
    public ResponseEntity<Long> getCommentsCount(@PathVariable Long postId) {
        Long count = commentService.getCommentsCount(postId);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/posts/{postId}/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteAllPostComments(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long postId) {
        // First verify if the current user owns the post (this check should be in the service layer)
        commentService.deleteAllPostComments(postId);
        return ResponseEntity.ok().build();
    }
}
