package com.training.repository;

import com.training.model.Department;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class DepartmentRepository {

    private final Map<Long, Department> departments = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Department save(Department department) {
        if (department.getId() == null) {
            department.setId(idCounter.getAndIncrement());
            department.setCreatedAt(LocalDateTime.now());
        }
        department.setUpdatedAt(LocalDateTime.now());
        departments.put(department.getId(), department);
        return department;
    }

    public Optional<Department> findById(Long id) {
        return Optional.ofNullable(departments.get(id));
    }

    public List<Department> findAll() {
        return new ArrayList<>(departments.values());
    }

    public void deleteById(Long id) {
        departments.remove(id);
    }

    public boolean existsById(Long id) {
        return departments.containsKey(id);
    }

    public boolean hasSufficientBudget(Long departmentId, BigDecimal amount) {
        return findById(departmentId)
                .map(dept -> dept.getAvailableBudget().compareTo(amount) >= 0)
                .orElse(false);
    }

    public void reserveBudget(Long departmentId, BigDecimal amount) {
        findById(departmentId).ifPresent(dept -> {
            dept.setReservedBudget(dept.getReservedBudget().add(amount));
            save(dept);
        });
    }

    public void releaseReservedBudget(Long departmentId, BigDecimal amount) {
        findById(departmentId).ifPresent(dept -> {
            dept.setReservedBudget(dept.getReservedBudget().subtract(amount));
            save(dept);
        });
    }

    public void convertReservedToUsed(Long departmentId, BigDecimal amount) {
        findById(departmentId).ifPresent(dept -> {
            dept.setReservedBudget(dept.getReservedBudget().subtract(amount));
            dept.setUsedBudget(dept.getUsedBudget().add(amount));
            save(dept);
        });
    }
}
