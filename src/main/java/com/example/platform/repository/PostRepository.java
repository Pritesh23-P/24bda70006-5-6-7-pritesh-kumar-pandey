package com.example.platform.repository;

import com.example.platform.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Scalable pagination and filtering
    Page<Post> findByCategoryIgnoreCase(String category, Pageable pageable);

    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE " +
            "(:category IS NULL OR LOWER(p.category) = LOWER(:category)) AND " +
            "(:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Post> searchPosts(@Param("category") String category, @Param("search") String search, Pageable pageable);

    // Optimized Query with JOIN FETCH to eliminate N+1 problem
    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN FETCH p.author a " +
            "LEFT JOIN FETCH p.comments c")
    List<Post> findAllWithJoinFetch();

    // Single post with JOIN FETCH
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.author a " +
            "LEFT JOIN FETCH p.comments c " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithAuthorAndComments(@Param("id") Long id);

    // Native SQL Query for complex aggregations
    @Query(value = "SELECT p.category AS category, COUNT(p.id) AS postCount, SUM(p.view_count) AS totalViews, AVG(p.view_count) AS avgViews " +
            "FROM posts p " +
            "GROUP BY p.category " +
            "ORDER BY totalViews DESC", nativeQuery = true)
    List<Object[]> getCategoryAnalyticsNative();
}
