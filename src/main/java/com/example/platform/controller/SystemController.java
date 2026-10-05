package com.example.platform.controller;

import com.example.platform.common.ApiResponse;
import com.example.platform.dto.BenchmarkResultDto;
import com.example.platform.exception.BadRequestException;
import com.example.platform.exception.ResourceNotFoundException;
import com.example.platform.service.SystemBenchmarkService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private static final Logger log = LoggerFactory.getLogger(SystemController.class);

    private final CacheManager cacheManager;
    private final SystemBenchmarkService benchmarkService;

    public SystemController(CacheManager cacheManager, SystemBenchmarkService benchmarkService) {
        this.cacheManager = cacheManager;
        this.benchmarkService = benchmarkService;
    }

    // --- Global Exception Handling Test Endpoints ---

    @GetMapping("/test-400")
    public ResponseEntity<ApiResponse<String>> triggerBadRequest() {
        log.info("Triggering 400 Bad Request test");
        throw new BadRequestException("Simulated 400 Bad Request exception for validation verification");
    }

    @GetMapping("/test-404")
    public ResponseEntity<ApiResponse<String>> triggerNotFound() {
        log.info("Triggering 404 Not Found test");
        throw new ResourceNotFoundException("Simulated 404: The requested resource was not located");
    }

    @GetMapping("/test-403")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> triggerAdminOnlyAction() {
        log.info("Admin-only action accessed");
        return ResponseEntity.ok(ApiResponse.success("Access Granted", "You have ADMIN privileges"));
    }

    @GetMapping("/test-500")
    public ResponseEntity<ApiResponse<String>> triggerInternalError() {
        log.info("Triggering 500 Internal Server Error test");
        throw new RuntimeException("Simulated unhandled system failure for global error handler verification");
    }

    // --- Correlation ID and Request Tracing ---

    @GetMapping("/trace-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTraceInfo() {
        String currentCorrelationId = MDC.get("correlationId");
        Map<String, Object> traceData = new HashMap<>();
        traceData.put("correlationId", currentCorrelationId);
        traceData.put("threadName", Thread.currentThread().getName());
        traceData.put("serverStatus", "UP");
        return ResponseEntity.ok(ApiResponse.success("Trace metadata retrieved", traceData));
    }

    // --- Caching Statistics & Cache Management ---

    @GetMapping("/cache-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        org.springframework.cache.Cache postSpringCache = cacheManager.getCache("posts");
        if (postSpringCache instanceof CaffeineCache) {
            Cache<Object, Object> nativeCache = ((CaffeineCache) postSpringCache).getNativeCache();
            CacheStats cStats = nativeCache.stats();
            stats.put("cacheName", "posts");
            stats.put("hitCount", cStats.hitCount());
            stats.put("missCount", cStats.missCount());
            stats.put("hitRate", String.format("%.2f%%", cStats.hitRate() * 100));
            stats.put("loadSuccessCount", cStats.loadSuccessCount());
            stats.put("estimatedSize", nativeCache.estimatedSize());
        } else {
            stats.put("cacheStatus", "Active (Generic)");
        }
        return ResponseEntity.ok(ApiResponse.success("Cache statistics retrieved", stats));
    }

    @PostMapping("/cache-clear")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        org.springframework.cache.Cache postsCache = cacheManager.getCache("posts");
        if (postsCache != null) {
            postsCache.clear();
        }
        log.info("Manual cache eviction executed for 'posts' cache");
        return ResponseEntity.ok(ApiResponse.success("Cache Cleared", "The 'posts' cache has been completely cleared"));
    }

    // --- N+1 Query vs JOIN FETCH Benchmark ---

    @GetMapping("/benchmark/n-plus-one")
    public ResponseEntity<ApiResponse<BenchmarkResultDto>> benchmarkUnoptimized() {
        BenchmarkResultDto result = benchmarkService.benchmarkUnoptimizedQuery();
        return ResponseEntity.ok(ApiResponse.success("Unoptimized N+1 query benchmark complete", result));
    }

    @GetMapping("/benchmark/join-fetch")
    public ResponseEntity<ApiResponse<BenchmarkResultDto>> benchmarkOptimized() {
        BenchmarkResultDto result = benchmarkService.benchmarkOptimizedQuery();
        return ResponseEntity.ok(ApiResponse.success("Optimized JOIN FETCH benchmark complete", result));
    }
}
