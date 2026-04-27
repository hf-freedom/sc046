package com.training.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgress {
    private Long id;
    private Long enrollmentId;
    private Long employeeId;
    private Long courseId;
    private Long chapterId;
    private Integer studyDurationMinutes;
    private boolean isCompleted;
    private LocalDateTime startDate;
    private LocalDateTime completionDate;
    private LocalDateTime lastActivityDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean meetsMinDuration(Integer minDurationMinutes) {
        return studyDurationMinutes >= minDurationMinutes;
    }
}
