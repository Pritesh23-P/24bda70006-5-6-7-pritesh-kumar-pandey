package com.example.platform.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ScheduleRequest {

    @NotNull(message = "Post ID is required")
    private Long postId;

    @NotBlank(message = "Platform is required")
    @Pattern(regexp = "^(TWITTER|LINKEDIN|FACEBOOK|INSTAGRAM)$", message = "Platform must be TWITTER, LINKEDIN, FACEBOOK, or INSTAGRAM")
    private String platform;

    @NotNull(message = "Scheduled time is required")
    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledTime;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    public ScheduleRequest() {
    }

    public ScheduleRequest(Long postId, String platform, LocalDateTime scheduledTime, String notes) {
        this.postId = postId;
        this.platform = platform;
        this.scheduledTime = scheduledTime;
        this.notes = notes;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
