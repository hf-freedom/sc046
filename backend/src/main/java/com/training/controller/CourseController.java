package com.training.controller;

import com.training.model.Chapter;
import com.training.model.Course;
import com.training.model.Exam;
import com.training.repository.ChapterRepository;
import com.training.repository.CourseRepository;
import com.training.repository.ExamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "http://localhost:3003")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private ExamRepository examRepository;

    @GetMapping
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @GetMapping("/published")
    public List<Course> getPublishedCourses() {
        return courseRepository.findPublishedCourses();
    }

    @GetMapping("/available")
    public List<Course> getAvailableCourses() {
        return courseRepository.findCoursesWithAvailableSlots();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return courseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{courseId}/chapters")
    public List<Chapter> getChaptersByCourseId(@PathVariable Long courseId) {
        return chapterRepository.findByCourseId(courseId);
    }

    @GetMapping("/{courseId}/exam")
    public ResponseEntity<Exam> getExamByCourseId(@PathVariable Long courseId) {
        return examRepository.findByCourseId(courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Course createCourse(@RequestBody Course course) {
        return courseRepository.save(course);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        if (!courseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        course.setId(id);
        return ResponseEntity.ok(courseRepository.save(course));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        if (!courseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        courseRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
