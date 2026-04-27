package com.training.repository;

import com.training.model.Exam;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ExamRepository {

    private final Map<Long, Exam> exams = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Exam save(Exam exam) {
        if (exam.getId() == null) {
            exam.setId(idCounter.getAndIncrement());
            exam.setCreatedAt(LocalDateTime.now());
        }
        exam.setUpdatedAt(LocalDateTime.now());
        exams.put(exam.getId(), exam);
        return exam;
    }

    public Optional<Exam> findById(Long id) {
        return Optional.ofNullable(exams.get(id));
    }

    public Optional<Exam> findByCourseId(Long courseId) {
        return exams.values().stream()
                .filter(e -> courseId.equals(e.getCourseId()))
                .findFirst();
    }

    public List<Exam> findAll() {
        return new ArrayList<>(exams.values());
    }

    public void deleteById(Long id) {
        exams.remove(id);
    }

    public void deleteByCourseId(Long courseId) {
        exams.entrySet().removeIf(entry -> 
            courseId.equals(entry.getValue().getCourseId()));
    }

    public boolean existsById(Long id) {
        return exams.containsKey(id);
    }
}
