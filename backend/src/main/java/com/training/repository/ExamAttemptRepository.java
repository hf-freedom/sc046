package com.training.repository;

import com.training.model.ExamAttempt;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ExamAttemptRepository {

    private final Map<Long, ExamAttempt> attempts = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public ExamAttempt save(ExamAttempt attempt) {
        if (attempt.getId() == null) {
            attempt.setId(idCounter.getAndIncrement());
            attempt.setCreatedAt(LocalDateTime.now());
        }
        attempt.setUpdatedAt(LocalDateTime.now());
        attempts.put(attempt.getId(), attempt);
        return attempt;
    }

    public Optional<ExamAttempt> findById(Long id) {
        return Optional.ofNullable(attempts.get(id));
    }

    public List<ExamAttempt> findByEnrollmentId(Long enrollmentId) {
        return attempts.values().stream()
                .filter(a -> enrollmentId.equals(a.getEnrollmentId()))
                .sorted(Comparator.comparingInt(ExamAttempt::getAttemptNumber).reversed())
                .collect(Collectors.toList());
    }

    public Optional<ExamAttempt> findLatestByEnrollmentId(Long enrollmentId) {
        return attempts.values().stream()
                .filter(a -> enrollmentId.equals(a.getEnrollmentId()))
                .max(Comparator.comparingInt(ExamAttempt::getAttemptNumber));
    }

    public int countAttemptsByEnrollmentId(Long enrollmentId) {
        return (int) attempts.values().stream()
                .filter(a -> enrollmentId.equals(a.getEnrollmentId()))
                .count();
    }

    public boolean hasPassed(Long enrollmentId) {
        return attempts.values().stream()
                .filter(a -> enrollmentId.equals(a.getEnrollmentId()))
                .anyMatch(a -> ExamAttempt.ExamAttemptStatus.PASSED.equals(a.getStatus()));
    }

    public List<ExamAttempt> findAll() {
        return new ArrayList<>(attempts.values());
    }

    public void deleteById(Long id) {
        attempts.remove(id);
    }

    public boolean existsById(Long id) {
        return attempts.containsKey(id);
    }
}
