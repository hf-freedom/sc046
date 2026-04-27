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
public class Department {
    private Long id;
    private String name;
    private String code;
    private BigDecimal annualBudget;
    private BigDecimal usedBudget;
    private BigDecimal reservedBudget;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BigDecimal getAvailableBudget() {
        return annualBudget.subtract(usedBudget).subtract(reservedBudget);
    }
}
