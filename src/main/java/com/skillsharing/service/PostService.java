package com.skillsharing.service;

import com.skillsharing.model.Post;
import com.skillsharing.model.User;
import com.skillsharing.repository.PostRepository;
import com.skillsharing.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Post createPost(Post post, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        // Validate media files (max 3)
        if (post.getMediaUrls() != null && post.getMediaUrls().size() > 3) {
            throw new IllegalArgumentException("Maximum 3 media files allowed per post");
        }
        
        post.setUser(user);
        return postRepository.save(post);
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));
    }

    public Post updatePost(Long postId, Post postDetails, Long userId) {
        Post post = getPostById(postId);
        
        // Verify ownership
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User does not own this post");
        }
        
        // Update fields
        post.setDescription(postDetails.getDescription());
        if (postDetails.getMediaUrls() != null && !postDetails.getMediaUrls().isEmpty()) {
            if (postDetails.getMediaUrls().size() > 3) {
                throw new IllegalArgumentException("Maximum 3 media files allowed per post");
            }
            post.setMediaUrls(postDetails.getMediaUrls());
        }
        
        // Update learning plan specific fields if applicable
        if (post.getPostType() == Post.PostType.LEARNING_PLAN) {
            post.setPlanTitle(postDetails.getPlanTitle());
            post.setPlanStartDate(postDetails.getPlanStartDate());
            post.setPlanEndDate(postDetails.getPlanEndDate());
            post.setPlanTopics(postDetails.getPlanTopics());
            post.setPlanResources(postDetails.getPlanResources());
        }
        
        // Update learning progress specific fields if applicable
        if (post.getPostType() == Post.PostType.LEARNING_PROGRESS) {
            post.setProgressTemplate(postDetails.getProgressTemplate());
            post.setProgressPercentage(postDetails.getProgressPercentage());
        }
        
        return postRepository.save(post);
    }

    public void deletePost(Long postId, Long userId) {
        Post post = getPostById(postId);
        
        // Verify ownership
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User does not own this post");
        }
        
        postRepository.delete(post);
    }

    public void likePost(Long postId, Long userId) {
        Post post = getPostById(postId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        if (!postRepository.hasUserLikedPost(postId, userId)) {
            post.getLikes().add(user);
            postRepository.save(post);
            
            // Create notification for post owner
            if (!post.getUser().getId().equals(userId)) {
                notificationService.createNotification(
                    post.getUser(),
                    user,
                    post,
                    "LIKE"
                );
            }
        }
    }

    public void unlikePost(Long postId, Long userId) {
        Post post = getPostById(postId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        post.getLikes().remove(user);
        postRepository.save(post);
    }

    public Page<Post> getUserPosts(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return postRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public Page<Post> getFollowingPosts(Long userId, Pageable pageable) {
        return postRepository.findFollowingUsersPosts(userId, pageable);
    }

    public Page<Post> searchPosts(String keyword, Pageable pageable) {
        return postRepository.searchPosts(keyword, pageable);
    }

    public List<Post> getActiveLearningPlans() {
        return postRepository.findActiveLearningPlans();
    }

    public Page<Post> getMostLikedPosts(Pageable pageable) {
        return postRepository.findMostLikedPosts(pageable);
    }

    public Page<Post> getMostCommentedPosts(Pageable pageable) {
        return postRepository.findMostCommentedPosts(pageable);
    }

    public Long getLikesCount(Long postId) {
        return postRepository.countLikesByPostId(postId);
    }

    public boolean hasUserLikedPost(Long postId, Long userId) {
        return postRepository.hasUserLikedPost(postId, userId);
    }
}
