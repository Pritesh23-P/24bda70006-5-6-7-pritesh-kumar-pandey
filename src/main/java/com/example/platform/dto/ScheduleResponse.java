package com.example.platform.dto;

import java.time.LocalDateTime;

public class ScheduleResponse {

    private Long id;
    private Long postId;
    private String postTitle;
    private String platform;
    private LocalDateTime scheduledTime;
    private String status;
    private String notes;
    private LocalDateTime createdAt;

    public ScheduleResponse() {
    }

    public ScheduleResponse(Long id, Long postId, String postTitle, String platform, LocalDateTime scheduledTime, String status, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.postTitle = postTitle;
        this.platform = platform;
        this.scheduledTime = scheduledTime;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
