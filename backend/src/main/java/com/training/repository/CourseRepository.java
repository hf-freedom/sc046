package com.training.repository;

import com.training.model.Course;
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
public class CourseRepository {

    private final Map<Long, Course> courses = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Course save(Course course) {
        if (course.getId() == null) {
            course.setId(idCounter.getAndIncrement());
            course.setCreatedAt(LocalDateTime.now());
            if (course.getCurrentParticipants() == null) {
                course.setCurrentParticipants(0);
            }
        }
        course.setUpdatedAt(LocalDateTime.now());
        courses.put(course.getId(), course);
        return course;
    }

    public Optional<Course> findById(Long id) {
        return Optional.ofNullable(courses.get(id));
    }

    public List<Course> findAll() {
        return new ArrayList<>(courses.values());
    }

    public List<Course> findPublishedCourses() {
        return courses.values().stream()
                .filter(c -> Course.CourseStatus.PUBLISHED.equals(c.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Course> findCoursesWithAvailableSlots() {
        return courses.values().stream()
                .filter(c -> Course.CourseStatus.PUBLISHED.equals(c.getStatus()))
                .filter(Course::hasAvailableSlots)
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        courses.remove(id);
    }

    public boolean existsById(Long id) {
        return courses.containsKey(id);
    }

    public boolean hasAvailableSlot(Long courseId) {
        return findById(courseId)
                .map(Course::hasAvailableSlots)
                .orElse(false);
    }

    public void incrementParticipants(Long courseId) {
        findById(courseId).ifPresent(course -> {
            course.setCurrentParticipants(course.getCurrentParticipants() + 1);
            save(course);
        });
    }

    public void decrementParticipants(Long courseId) {
        findById(courseId).ifPresent(course -> {
            if (course.getCurrentParticipants() > 0) {
                course.setCurrentParticipants(course.getCurrentParticipants() - 1);
                save(course);
            }
        });
    }
}
