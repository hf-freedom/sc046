package com.training.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestion {
    private Long id;
    private String questionText;
    private List<String> options;
    private Integer correctAnswerIndex;
    private Integer points;
}
