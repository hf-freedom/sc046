package com.training.controller;

import com.training.model.Exam;
import com.training.model.ExamAttempt;
import com.training.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/exams")
@CrossOrigin(origins = "http://localhost:3003")
public class ExamController {

    @Autowired
    private ExamService examService;

    @PostMapping("/start")
    public ResponseEntity<?> startExam(@RequestParam Long enrollmentId) {
        try {
            ExamAttempt attempt = examService.startExam(enrollmentId);
            return ResponseEntity.ok(attempt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/submit/{attemptId}")
    public ResponseEntity<?> submitExam(
            @PathVariable Long attemptId,
            @RequestBody List<Integer> answers) {
        try {
            ExamAttempt attempt = examService.submitExam(attemptId, answers);
            return ResponseEntity.ok(attempt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Exam> getExamByCourseId(@PathVariable Long courseId) {
        return examService.findByCourseId(courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<ExamAttempt> getAttemptById(@PathVariable Long attemptId) {
        return examService.findById(attemptId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/attempts/enrollment/{enrollmentId}")
    public List<ExamAttempt> getAttemptsByEnrollment(@PathVariable Long enrollmentId) {
        return examService.findAttemptsByEnrollmentId(enrollmentId);
    }

    @GetMapping("/attempts/latest/{enrollmentId}")
    public ResponseEntity<ExamAttempt> getLatestAttempt(@PathVariable Long enrollmentId) {
        return examService.findLatestAttemptByEnrollmentId(enrollmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/remaining-attempts/{enrollmentId}")
    public ResponseEntity<?> getRemainingAttempts(@PathVariable Long enrollmentId) {
        try {
            int remaining = examService.getRemainingAttempts(enrollmentId);
            return ResponseEntity.ok(remaining);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
