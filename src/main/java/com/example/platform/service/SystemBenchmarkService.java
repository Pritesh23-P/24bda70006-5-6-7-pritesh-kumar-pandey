package com.example.platform.service;

import com.example.platform.dto.BenchmarkResultDto;
import com.example.platform.dto.PostResponse;
import com.example.platform.model.Post;
import com.example.platform.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SystemBenchmarkService {

    private static final Logger log = LoggerFactory.getLogger(SystemBenchmarkService.class);

    private final PostRepository postRepository;
    private final PostService postService;

    public SystemBenchmarkService(PostRepository postRepository, PostService postService) {
        this.postRepository = postRepository;
        this.postService = postService;
    }

    @Transactional(readOnly = true)
    public BenchmarkResultDto benchmarkUnoptimizedQuery() {
        long start = System.currentTimeMillis();

        // Unoptimized: findAll() triggers 1 query for posts, then lazy load for each author & comments (N + 1 problem)
        List<Post> posts = postRepository.findAll();
        List<PostResponse> responses = new ArrayList<>();
        for (Post post : posts) {
            responses.add(postService.mapToResponse(post));
        }

        long duration = System.currentTimeMillis() - start;
        int count = posts.size();
        int estimatedQueries = 1 + count * 2; // 1 post query + N author queries + N comment queries

        log.info("Benchmark Unoptimized (N+1): {} posts processed in {}ms with ~{} queries", count, duration, estimatedQueries);

        return new BenchmarkResultDto(
                "Unoptimized (Standard JPA / N+1 Latency)",
                count,
                duration,
                estimatedQueries,
                "Fetches posts first, then executes separate queries on demand for each post author and comment collection."
        );
    }

    @Transactional(readOnly = true)
    public BenchmarkResultDto benchmarkOptimizedQuery() {
        long start = System.currentTimeMillis();

        // Optimized: Single SQL query with JOIN FETCH
        List<Post> posts = postRepository.findAllWithJoinFetch();
        List<PostResponse> responses = new ArrayList<>();
        for (Post post : posts) {
            responses.add(postService.mapToResponse(post));
        }

        long duration = System.currentTimeMillis() - start;
        int count = posts.size();

        log.info("Benchmark Optimized (JOIN FETCH): {} posts processed in {}ms with 1 query", count, duration);

        return new BenchmarkResultDto(
                "Optimized (JOIN FETCH Single Query)",
                count,
                duration,
                1,
                "Uses JOIN FETCH to retrieve all posts along with authors and comments in a single database round-trip."
        );
    }
}
