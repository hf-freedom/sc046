package com.training.repository;

import com.training.model.Enrollment;
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
public class EnrollmentRepository {

    private final Map<Long, Enrollment> enrollments = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Enrollment save(Enrollment enrollment) {
        if (enrollment.getId() == null) {
            enrollment.setId(idCounter.getAndIncrement());
            enrollment.setCreatedAt(LocalDateTime.now());
        }
        enrollment.setUpdatedAt(LocalDateTime.now());
        enrollments.put(enrollment.getId(), enrollment);
        return enrollment;
    }

    public Optional<Enrollment> findById(Long id) {
        return Optional.ofNullable(enrollments.get(id));
    }

    public List<Enrollment> findAll() {
        return new ArrayList<>(enrollments.values());
    }

    public Optional<Enrollment> findByEmployeeIdAndCourseId(Long employeeId, Long courseId) {
        return enrollments.values().stream()
                .filter(e -> employeeId.equals(e.getEmployeeId()))
                .filter(e -> courseId.equals(e.getCourseId()))
                .filter(e -> !Enrollment.EnrollmentStatus.WITHDRAWN.equals(e.getStatus()))
                .findFirst();
    }

    public List<Enrollment> findByEmployeeId(Long employeeId) {
        return enrollments.values().stream()
                .filter(e -> employeeId.equals(e.getEmployeeId()))
                .collect(Collectors.toList());
    }

    public List<Enrollment> findByCourseId(Long courseId) {
        return enrollments.values().stream()
                .filter(e -> courseId.equals(e.getCourseId()))
                .collect(Collectors.toList());
    }

    public List<Enrollment> findByDepartmentId(Long departmentId) {
        return enrollments.values().stream()
                .filter(e -> departmentId.equals(e.getDepartmentId()))
                .collect(Collectors.toList());
    }

    public List<Enrollment> findActiveEnrollments() {
        return enrollments.values().stream()
                .filter(e -> Enrollment.EnrollmentStatus.ENROLLED.equals(e.getStatus()) 
                        || Enrollment.EnrollmentStatus.IN_PROGRESS.equals(e.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Enrollment> findCompletedEnrollments() {
        return enrollments.values().stream()
                .filter(e -> Enrollment.EnrollmentStatus.COMPLETED.equals(e.getStatus())
                        || Enrollment.EnrollmentStatus.CERTIFIED.equals(e.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Enrollment> findByStatus(Enrollment.EnrollmentStatus status) {
        return enrollments.values().stream()
                .filter(e -> status.equals(e.getStatus()))
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        enrollments.remove(id);
    }

    public boolean existsById(Long id) {
        return enrollments.containsKey(id);
    }

    public boolean isAlreadyEnrolled(Long employeeId, Long courseId) {
        return enrollments.values().stream()
                .filter(e -> employeeId.equals(e.getEmployeeId()))
                .filter(e -> courseId.equals(e.getCourseId()))
                .anyMatch(e -> !Enrollment.EnrollmentStatus.WITHDRAWN.equals(e.getStatus())
                        && !Enrollment.EnrollmentStatus.FAILED.equals(e.getStatus()));
    }
}
