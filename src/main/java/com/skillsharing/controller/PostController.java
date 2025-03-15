package com.skillsharing.controller;

import com.skillsharing.model.Post;
import com.skillsharing.security.CurrentUser;
import com.skillsharing.security.UserPrincipal;
import com.skillsharing.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Post> createPost(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody Post post) {
        Post newPost = postService.createPost(post, currentUser.getId());
        return ResponseEntity.ok(newPost);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Post> updatePost(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody Post postDetails) {
        Post updatedPost = postService.updatePost(id, postDetails, currentUser.getId());
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deletePost(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        postService.deletePost(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> likePost(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        postService.likePost(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unlike")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> unlikePost(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        postService.unlikePost(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Post>> getUserPosts(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<Post> posts = postService.getUserPosts(userId, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/feed")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<Post>> getFeed(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        Page<Post> posts = postService.getFollowingPosts(currentUser.getId(), pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Post>> searchPosts(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<Post> posts = postService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/learning-plans/active")
    public ResponseEntity<List<Post>> getActiveLearningPlans() {
        List<Post> learningPlans = postService.getActiveLearningPlans();
        return ResponseEntity.ok(learningPlans);
    }

    @GetMapping("/trending")
    public ResponseEntity<Page<Post>> getTrendingPosts(Pageable pageable) {
        Page<Post> posts = postService.getMostLikedPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/most-discussed")
    public ResponseEntity<Page<Post>> getMostDiscussedPosts(Pageable pageable) {
        Page<Post> posts = postService.getMostCommentedPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}/likes-count")
    public ResponseEntity<Long> getLikesCount(@PathVariable Long id) {
        Long likesCount = postService.getLikesCount(id);
        return ResponseEntity.ok(likesCount);
    }

    @GetMapping("/{id}/has-liked")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> hasUserLikedPost(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        boolean hasLiked = postService.hasUserLikedPost(id, currentUser.getId());
        return ResponseEntity.ok(hasLiked);
    }
}
