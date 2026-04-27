package com.training.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {
    private Long id;
    private String certificateNo;
    private Long employeeId;
    private Long courseId;
    private Long examAttemptId;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private CertificateStatus status;
    private boolean reminderSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum CertificateStatus {
        VALID,
        EXPIRING_SOON,
        EXPIRED,
        REVOKED
    }

    public boolean isExpiringSoon() {
        if (expiryDate == null) return false;
        LocalDate now = LocalDate.now();
        return now.plusMonths(1).isAfter(expiryDate) && now.isBefore(expiryDate);
    }
}
