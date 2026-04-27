package com.training.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAttempt {
    private Long id;
    private Long enrollmentId;
    private Long employeeId;
    private Long examId;
    private Integer attemptNumber;
    private ExamAttemptStatus status;
    private Integer score;
    private List<ExamAnswer> answers;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ExamAttemptStatus {
        IN_PROGRESS,
        COMPLETED,
        PASSED,
        FAILED
    }
}
