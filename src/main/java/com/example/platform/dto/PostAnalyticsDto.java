package com.example.platform.dto;

public class PostAnalyticsDto {

    private String category;
    private Long postCount;
    private Long totalViews;
    private Double avgViews;

    public PostAnalyticsDto() {
    }

    public PostAnalyticsDto(String category, Long postCount, Long totalViews, Double avgViews) {
        this.category = category;
        this.postCount = postCount;
        this.totalViews = totalViews;
        this.avgViews = avgViews;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getPostCount() {
        return postCount;
    }

    public void setPostCount(Long postCount) {
        this.postCount = postCount;
    }

    public Long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(Long totalViews) {
        this.totalViews = totalViews;
    }

    public Double getAvgViews() {
        return avgViews;
    }

    public void setAvgViews(Double avgViews) {
        this.avgViews = avgViews;
    }
}
