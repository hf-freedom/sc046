package com.training.service;

import com.training.model.*;
import com.training.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LearningProgressRepository learningProgressRepository;

    @Transactional
    public Enrollment enroll(Long employeeId, Long courseId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在"));

        if (!Employee.EmployeeStatus.ACTIVE.equals(employee.getStatus())) {
            throw new IllegalArgumentException("员工状态不允许报名");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));

        if (!Course.CourseStatus.PUBLISHED.equals(course.getStatus())) {
            throw new IllegalArgumentException("课程未发布");
        }

        if (!course.hasAvailableSlots()) {
            throw new IllegalArgumentException("课程名额已满");
        }

        if (enrollmentRepository.isAlreadyEnrolled(employeeId, courseId)) {
            throw new IllegalArgumentException("员工已报名该课程");
        }

        Long departmentId = employee.getDepartmentId();
        if (!departmentRepository.hasSufficientBudget(departmentId, course.getCourseFee())) {
            throw new IllegalArgumentException("部门预算不足");
        }

        departmentRepository.reserveBudget(departmentId, course.getCourseFee());
        courseRepository.incrementParticipants(courseId);

        Enrollment enrollment = Enrollment.builder()
                .employeeId(employeeId)
                .courseId(courseId)
                .departmentId(departmentId)
                .status(Enrollment.EnrollmentStatus.ENROLLED)
                .reservedFee(course.getCourseFee())
                .enrollmentDate(LocalDateTime.now())
                .build();

        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment withdraw(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("报名记录不存在"));

        if (Enrollment.EnrollmentStatus.WITHDRAWN.equals(enrollment.getStatus())) {
            throw new IllegalArgumentException("该报名已退课");
        }

        if (Enrollment.EnrollmentStatus.COMPLETED.equals(enrollment.getStatus())
                || Enrollment.EnrollmentStatus.CERTIFIED.equals(enrollment.getStatus())) {
            throw new IllegalArgumentException("已完成的课程无法退课");
        }

        Course course = courseRepository.findById(enrollment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));

        BigDecimal refundAmount = BigDecimal.ZERO;
        if (!enrollment.hasStartedLearning()) {
            refundAmount = enrollment.getReservedFee();
            departmentRepository.releaseReservedBudget(enrollment.getDepartmentId(), refundAmount);
        } else {
            BigDecimal completedRatio = calculateCompletionRatio(enrollment);
            BigDecimal usedFee = course.getCourseFee().multiply(completedRatio)
                    .setScale(2, RoundingMode.HALF_UP);
            refundAmount = course.getCourseFee().subtract(usedFee);

            if (usedFee.compareTo(BigDecimal.ZERO) > 0) {
                departmentRepository.convertReservedToUsed(enrollment.getDepartmentId(), usedFee);
            }
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                departmentRepository.releaseReservedBudget(enrollment.getDepartmentId(), refundAmount);
            }
        }

        courseRepository.decrementParticipants(course.getId());

        enrollment.setStatus(Enrollment.EnrollmentStatus.WITHDRAWN);
        enrollment.setWithdrawalDate(LocalDateTime.now());
        enrollment.setRefundAmount(refundAmount);
        enrollment.setActualFee(enrollment.getReservedFee().subtract(refundAmount));

        return enrollmentRepository.save(enrollment);
    }

    private BigDecimal calculateCompletionRatio(Enrollment enrollment) {
        List<LearningProgress> progressList = learningProgressRepository.findByEnrollmentId(enrollment.getId());
        if (progressList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Course course = courseRepository.findById(enrollment.getCourseId()).orElse(null);
        if (course == null || course.getTotalChapters() == 0) {
            return BigDecimal.ZERO;
        }

        long completedChapters = progressList.stream()
                .filter(LearningProgress::isCompleted)
                .count();

        return BigDecimal.valueOf(completedChapters)
                .divide(BigDecimal.valueOf(course.getTotalChapters()), 4, RoundingMode.HALF_UP);
    }

    public Optional<Enrollment> findById(Long id) {
        return enrollmentRepository.findById(id);
    }

    public List<Enrollment> findByEmployeeId(Long employeeId) {
        return enrollmentRepository.findByEmployeeId(employeeId);
    }

    public List<Enrollment> findByCourseId(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    public Enrollment updateStatus(Long enrollmentId, Enrollment.EnrollmentStatus newStatus) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("报名记录不存在"));
        enrollment.setStatus(newStatus);
        return enrollmentRepository.save(enrollment);
    }
}
