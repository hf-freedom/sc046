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
public class Chapter {
    private Long id;
    private Long courseId;
    private String title;
    private String content;
    private Integer chapterOrder;
    private Integer minStudyDurationMinutes;
    private boolean isMandatory;
    private List<Long> prerequisiteChapterIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
