package com.example.platform.repository;

import com.example.platform.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByStatusOrderByScheduledTimeAsc(String status);
    List<Schedule> findByPostId(Long postId);
}
