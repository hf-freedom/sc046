package com.training.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    private Long id;
    private String name;
    private String code;
    private String description;
    private BigDecimal courseFee;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Integer totalChapters;
    private CourseStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum CourseStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }

    public boolean hasAvailableSlots() {
        return currentParticipants < maxParticipants;
    }

    public int getAvailableSlots() {
        return maxParticipants - currentParticipants;
    }
}
