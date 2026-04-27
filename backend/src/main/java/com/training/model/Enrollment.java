package com.training.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    private Long id;
    private Long employeeId;
    private Long courseId;
    private Long departmentId;
    private EnrollmentStatus status;
    private BigDecimal reservedFee;
    private BigDecimal actualFee;
    private LocalDateTime enrollmentDate;
    private LocalDateTime startDate;
    private LocalDateTime completionDate;
    private LocalDateTime withdrawalDate;
    private BigDecimal refundAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum EnrollmentStatus {
        PENDING,
        ENROLLED,
        IN_PROGRESS,
        COMPLETED,
        CERTIFIED,
        WITHDRAWN,
        FAILED
    }

    public boolean hasStartedLearning() {
        return startDate != null;
    }
}
