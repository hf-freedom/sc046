package com.training.repository;

import com.training.model.LearningProgress;
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
public class LearningProgressRepository {

    private final Map<Long, LearningProgress> progressMap = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public LearningProgress save(LearningProgress progress) {
        if (progress.getId() == null) {
            progress.setId(idCounter.getAndIncrement());
            progress.setCreatedAt(LocalDateTime.now());
        }
        progress.setUpdatedAt(LocalDateTime.now());
        progressMap.put(progress.getId(), progress);
        return progress;
    }

    public Optional<LearningProgress> findById(Long id) {
        return Optional.ofNullable(progressMap.get(id));
    }

    public Optional<LearningProgress> findByEnrollmentIdAndChapterId(Long enrollmentId, Long chapterId) {
        return progressMap.values().stream()
                .filter(p -> enrollmentId.equals(p.getEnrollmentId()))
                .filter(p -> chapterId.equals(p.getChapterId()))
                .findFirst();
    }

    public List<LearningProgress> findByEnrollmentId(Long enrollmentId) {
        return progressMap.values().stream()
                .filter(p -> enrollmentId.equals(p.getEnrollmentId()))
                .collect(Collectors.toList());
    }

    public List<LearningProgress> findByEmployeeId(Long employeeId) {
        return progressMap.values().stream()
                .filter(p -> employeeId.equals(p.getEmployeeId()))
                .collect(Collectors.toList());
    }

    public List<LearningProgress> findByEmployeeIdAndCourseId(Long employeeId, Long courseId) {
        return progressMap.values().stream()
                .filter(p -> employeeId.equals(p.getEmployeeId()))
                .filter(p -> courseId.equals(p.getCourseId()))
                .collect(Collectors.toList());
    }

    public List<LearningProgress> findCompletedByEnrollmentId(Long enrollmentId) {
        return progressMap.values().stream()
                .filter(p -> enrollmentId.equals(p.getEnrollmentId()))
                .filter(LearningProgress::isCompleted)
                .collect(Collectors.toList());
    }

    public List<LearningProgress> findAll() {
        return new ArrayList<>(progressMap.values());
    }

    public void deleteById(Long id) {
        progressMap.remove(id);
    }

    public void deleteByEnrollmentId(Long enrollmentId) {
        progressMap.entrySet().removeIf(entry -> 
            enrollmentId.equals(entry.getValue().getEnrollmentId()));
    }

    public boolean existsById(Long id) {
        return progressMap.containsKey(id);
    }
}
