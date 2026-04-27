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
public class Exam {
    private Long id;
    private Long courseId;
    private String name;
    private Integer totalQuestions;
    private Integer passingScore;
    private Integer durationMinutes;
    private Integer maxRetakeAttempts;
    private List<ExamQuestion> questions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
