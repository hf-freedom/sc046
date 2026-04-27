package com.training.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    private Long id;
    private String name;
    private String employeeNo;
    private Long departmentId;
    private String position;
    private String email;
    private String phone;
    private EmployeeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum EmployeeStatus {
        ACTIVE,
        INACTIVE,
        ON_LEAVE,
        TERMINATED
    }
}
