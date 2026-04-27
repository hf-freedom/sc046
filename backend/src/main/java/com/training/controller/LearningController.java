package com.training.controller;

import com.training.model.LearningProgress;
import com.training.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
@CrossOrigin(origins = "http://localhost:3003")
public class LearningController {

    @Autowired
    private LearningService learningService;

    @PostMapping("/start")
    public ResponseEntity<?> startLearning(
            @RequestParam Long enrollmentId,
            @RequestParam Long chapterId) {
        try {
            LearningProgress progress = learningService.startLearning(enrollmentId, chapterId);
            return ResponseEntity.ok(progress);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/progress")
    public ResponseEntity<?> updateProgress(
            @RequestParam Long enrollmentId,
            @RequestParam Long chapterId,
            @RequestParam int additionalMinutes) {
        try {
            LearningProgress progress = learningService.updateProgress(enrollmentId, chapterId, additionalMinutes);
            return ResponseEntity.ok(progress);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeChapter(
            @RequestParam Long enrollmentId,
            @RequestParam Long chapterId) {
        try {
            LearningProgress progress = learningService.completeChapter(enrollmentId, chapterId);
            return ResponseEntity.ok(progress);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/enrollment/{enrollmentId}")
    public List<LearningProgress> getProgressByEnrollment(@PathVariable Long enrollmentId) {
        return learningService.findByEnrollmentId(enrollmentId);
    }

    @GetMapping("/employee/{employeeId}/course/{courseId}")
    public List<LearningProgress> getProgressByEmployeeAndCourse(
            @PathVariable Long employeeId,
            @PathVariable Long courseId) {
        return learningService.findByEmployeeIdAndCourseId(employeeId, courseId);
    }

    @GetMapping("/can-take-exam/{enrollmentId}")
    public ResponseEntity<Boolean> canTakeExam(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(learningService.canTakeExam(enrollmentId));
    }
}
