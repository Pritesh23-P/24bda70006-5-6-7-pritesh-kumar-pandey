package com.example.platform.service;

import com.example.platform.common.PagedResponse;
import com.example.platform.dto.CommentDto;
import com.example.platform.dto.PostAnalyticsDto;
import com.example.platform.dto.PostRequest;
import com.example.platform.dto.PostResponse;
import com.example.platform.exception.ResourceNotFoundException;
import com.example.platform.exception.UnauthorizedException;
import com.example.platform.model.Comment;
import com.example.platform.model.Post;
import com.example.platform.model.User;
import com.example.platform.repository.CommentRepository;
import com.example.platform.repository.PostRepository;
import com.example.platform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public PostResponse createPost(PostRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Post post = new Post(
                request.getTitle(),
                request.getContent(),
                request.getCategory(),
                request.getTags(),
                user
        );

        Post savedPost = postRepository.save(post);
        log.info("Created post ID {} by author {}", savedPost.getId(), username);
        return mapToResponse(savedPost);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getAllPosts(int page, int size, String sortBy, String sortDir, String category, String search) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> postsPage = postRepository.searchPosts(
                (category != null && !category.trim().isEmpty()) ? category.trim() : null,
                (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                pageable
        );

        List<PostResponse> content = postsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                postsPage.getNumber(),
                postsPage.getSize(),
                postsPage.getTotalElements(),
                postsPage.getTotalPages(),
                postsPage.isLast(),
                sortBy,
                sortDir
        );
    }

    @Transactional
    @Cacheable(value = "posts", key = "#id")
    public PostResponse getPostById(Long id) {
        log.info("Cache miss for post ID {}. Querying database...", id);
        Post post = postRepository.findByIdWithAuthorAndComments(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + id));

        // Increment view count
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        return mapToResponse(post);
    }

    @Transactional
    @CacheEvict(value = "posts", key = "#id")
    public PostResponse updatePost(Long id, PostRequest request, String currentUsername, boolean isAdmin) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + id));

        if (!post.getAuthor().getUsername().equals(currentUsername) && !isAdmin) {
            throw new UnauthorizedException("You do not have permission to edit this post");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        post.setTags(request.getTags());

        Post updatedPost = postRepository.save(post);
        log.info("Updated post ID {} by user {}", id, currentUsername);
        return mapToResponse(updatedPost);
    }

    @Transactional
    @CacheEvict(value = "posts", key = "#id")
    public void deletePost(Long id, String currentUsername, boolean isAdmin) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + id));

        if (!post.getAuthor().getUsername().equals(currentUsername) && !isAdmin) {
            throw new UnauthorizedException("You do not have permission to delete this post");
        }

        postRepository.delete(post);
        log.info("Deleted post ID {} by user {}", id, currentUsername);
    }

    @Transactional
    @CacheEvict(value = "posts", key = "#postId")
    public PostResponse addComment(Long postId, String content, String authorName) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        Comment comment = new Comment(content, authorName, post);
        commentRepository.save(comment);

        post.getComments().add(comment);
        log.info("Added comment to post ID {} by {}", postId, authorName);
        return mapToResponse(post);
    }

    @Transactional(readOnly = true)
    public List<PostAnalyticsDto> getCategoryAnalyticsNative() {
        List<Object[]> rows = postRepository.getCategoryAnalyticsNative();
        List<PostAnalyticsDto> results = new ArrayList<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            Long postCount = ((Number) row[1]).longValue();
            Long totalViews = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Double avgViews = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            results.add(new PostAnalyticsDto(category, postCount, totalViews, avgViews));
        }
        return results;
    }

    public PostResponse mapToResponse(Post post) {
        List<CommentDto> commentDtos = post.getComments() != null
                ? post.getComments().stream()
                .map(c -> new CommentDto(c.getId(), c.getContent(), c.getAuthorName(), c.getCreatedAt()))
                .collect(Collectors.toList())
                : new ArrayList<>();

        String authorName = post.getAuthor() != null ? post.getAuthor().getUsername() : "Unknown";

        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getTags(),
                authorName,
                post.getViewCount(),
                commentDtos.size(),
                commentDtos,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
