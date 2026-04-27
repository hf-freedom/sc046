package com.training.repository;

import com.training.model.Employee;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class EmployeeRepository {

    private final Map<Long, Employee> employees = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Employee save(Employee employee) {
        if (employee.getId() == null) {
            employee.setId(idCounter.getAndIncrement());
            employee.setCreatedAt(LocalDateTime.now());
        }
        employee.setUpdatedAt(LocalDateTime.now());
        employees.put(employee.getId(), employee);
        return employee;
    }

    public Optional<Employee> findById(Long id) {
        return Optional.ofNullable(employees.get(id));
    }

    public List<Employee> findAll() {
        return new ArrayList<>(employees.values());
    }

    public List<Employee> findByDepartmentId(Long departmentId) {
        return employees.values().stream()
                .filter(e -> departmentId.equals(e.getDepartmentId()))
                .collect(Collectors.toList());
    }

    public Optional<Employee> findByEmployeeNo(String employeeNo) {
        return employees.values().stream()
                .filter(e -> employeeNo.equals(e.getEmployeeNo()))
                .findFirst();
    }

    public void deleteById(Long id) {
        employees.remove(id);
    }

    public boolean existsById(Long id) {
        return employees.containsKey(id);
    }

    public boolean isActive(Long employeeId) {
        return findById(employeeId)
                .map(e -> Employee.EmployeeStatus.ACTIVE.equals(e.getStatus()))
                .orElse(false);
    }
}
