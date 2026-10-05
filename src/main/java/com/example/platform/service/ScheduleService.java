package com.example.platform.service;

import com.example.platform.dto.ScheduleRequest;
import com.example.platform.dto.ScheduleResponse;
import com.example.platform.exception.BadRequestException;
import com.example.platform.exception.ResourceNotFoundException;
import com.example.platform.model.Post;
import com.example.platform.model.Schedule;
import com.example.platform.repository.PostRepository;
import com.example.platform.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

    private final ScheduleRepository scheduleRepository;
    private final PostRepository postRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, PostRepository postRepository) {
        this.scheduleRepository = scheduleRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public ScheduleResponse createSchedule(ScheduleRequest request) {
        if (request.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Scheduled time must be in the future");
        }

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + request.getPostId()));

        Schedule schedule = new Schedule(
                post,
                request.getPlatform().toUpperCase(),
                request.getScheduledTime(),
                request.getNotes()
        );

        Schedule saved = scheduleRepository.save(schedule);
        log.info("Created schedule ID {} for post '{}' on platform {}", saved.getId(), post.getTitle(), saved.getPlatform());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleById(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with ID: " + id));
        return mapToResponse(schedule);
    }

    @Transactional
    public ScheduleResponse updateStatus(Long id, String status) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with ID: " + id));

        schedule.setStatus(status.toUpperCase());
        Schedule saved = scheduleRepository.save(schedule);
        log.info("Updated schedule ID {} status to {}", id, status);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with ID: " + id));
        scheduleRepository.delete(schedule);
        log.info("Deleted schedule ID {}", id);
    }

    private ScheduleResponse mapToResponse(Schedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getPost().getId(),
                schedule.getPost().getTitle(),
                schedule.getPlatform(),
                schedule.getScheduledTime(),
                schedule.getStatus(),
                schedule.getNotes(),
                schedule.getCreatedAt()
        );
    }
}
