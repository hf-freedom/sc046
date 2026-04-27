package com.training.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnswer {
    private Long questionId;
    private Integer selectedAnswerIndex;
    private boolean isCorrect;
}
