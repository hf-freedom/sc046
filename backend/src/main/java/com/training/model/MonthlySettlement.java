package com.training.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySettlement {
    private Long id;
    private Long departmentId;
    private YearMonth settlementMonth;
    private BigDecimal totalEnrolledFee;
    private BigDecimal totalCompletedFee;
    private BigDecimal totalWithdrawnFee;
    private BigDecimal netExpense;
    private SettlementStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum SettlementStatus {
        PENDING,
        CALCULATED,
        APPROVED,
        SETTLED
    }
}
