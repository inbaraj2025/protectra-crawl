package com.protectra.sp.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.protectra.sp.entity.Schedule;

public interface ScheduleRepository extends MongoRepository<Schedule, String> {

    // Custom query to find all schedules by assetId
    List<Schedule> findByAssetId(String assetId);

    // Custom query to find all schedules that are due (for scheduled time in the past)
    List<Schedule> findByScheduledTimeBefore(LocalDateTime currentTime);
}
