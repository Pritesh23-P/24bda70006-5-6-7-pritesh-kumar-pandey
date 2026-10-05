package com.example.platform.controller;

import com.example.platform.common.ApiResponse;
import com.example.platform.common.PagedResponse;
import com.example.platform.dto.PostAnalyticsDto;
import com.example.platform.dto.PostRequest;
import com.example.platform.dto.PostResponse;
import com.example.platform.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PostResponse response = postService.createPost(request, userDetails.getUsername());
        return new ResponseEntity<>(ApiResponse.success("Post created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        PagedResponse<PostResponse> response = postService.getAllPosts(page, size, sortBy, sortDir, category, search);
        return ResponseEntity.ok(ApiResponse.success("Posts retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long id) {
        PostResponse response = postService.getPostById(id);
        return ResponseEntity.ok(ApiResponse.success("Post retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        PostResponse response = postService.updatePost(id, request, userDetails.getUsername(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Post updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        postService.deletePost(id, userDetails.getUsername(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully", "Post ID " + id + " has been removed"));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        String content = payload.get("content");
        if (content == null || content.trim().isEmpty()) {
            return new ResponseEntity<>(ApiResponse.error("Comment content cannot be empty"), HttpStatus.BAD_REQUEST);
        }
        PostResponse response = postService.addComment(id, content.trim(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Comment added successfully", response));
    }

    @GetMapping("/analytics/native")
    public ResponseEntity<ApiResponse<List<PostAnalyticsDto>>> getAnalyticsNative() {
        List<PostAnalyticsDto> analytics = postService.getCategoryAnalyticsNative();
        return ResponseEntity.ok(ApiResponse.success("Category analytics retrieved using Native SQL query", analytics));
    }
}
