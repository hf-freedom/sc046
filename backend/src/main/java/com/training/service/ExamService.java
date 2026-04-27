package com.training.service;

import com.training.model.*;
import com.training.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamAttemptRepository attemptRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LearningService learningService;

    @Autowired
    private CertificateService certificateService;

    @Transactional
    public ExamAttempt startExam(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("报名记录不存在"));

        if (!learningService.canTakeExam(enrollmentId)) {
            throw new IllegalArgumentException("未完成必修章节，无法参加考试");
        }

        Exam exam = examRepository.findByCourseId(enrollment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("课程未配置考试"));

        int attemptCount = attemptRepository.countAttemptsByEnrollmentId(enrollmentId);
        
        if (attemptCount > 0) {
            Optional<ExamAttempt> lastAttempt = attemptRepository.findLatestByEnrollmentId(enrollmentId);
            if (lastAttempt.isPresent() && 
                ExamAttempt.ExamAttemptStatus.IN_PROGRESS.equals(lastAttempt.get().getStatus())) {
                throw new IllegalArgumentException("存在进行中的考试，请先完成或取消");
            }

            if (attemptRepository.hasPassed(enrollmentId)) {
                throw new IllegalArgumentException("已通过考试，无需重考");
            }

            if (attemptCount > exam.getMaxRetakeAttempts()) {
                throw new IllegalArgumentException("已超过最大补考次数");
            }
        }

        ExamAttempt attempt = ExamAttempt.builder()
                .enrollmentId(enrollmentId)
                .employeeId(enrollment.getEmployeeId())
                .examId(exam.getId())
                .attemptNumber(attemptCount + 1)
                .status(ExamAttempt.ExamAttemptStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();

        return attemptRepository.save(attempt);
    }

    @Transactional
    public ExamAttempt submitExam(Long attemptId, List<Integer> answers) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("考试记录不存在"));

        if (!ExamAttempt.ExamAttemptStatus.IN_PROGRESS.equals(attempt.getStatus())) {
            throw new IllegalArgumentException("考试不在进行中");
        }

        Exam exam = examRepository.findById(attempt.getExamId())
                .orElseThrow(() -> new IllegalArgumentException("考试不存在"));

        if (exam.getQuestions() == null || exam.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("考试题目配置错误");
        }

        if (answers.size() != exam.getQuestions().size()) {
            throw new IllegalArgumentException("答案数量不匹配");
        }

        int totalScore = 0;
        List<ExamQuestion> questions = exam.getQuestions();
        List<ExamAnswer> examAnswers = new java.util.ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            ExamQuestion question = questions.get(i);
            int selectedAnswer = answers.get(i);
            boolean isCorrect = selectedAnswer == question.getCorrectAnswerIndex();
            if (isCorrect) {
                totalScore += question.getPoints();
            }
            examAnswers.add(ExamAnswer.builder()
                    .questionId(question.getId())
                    .selectedAnswerIndex(selectedAnswer)
                    .isCorrect(isCorrect)
                    .build());
        }

        attempt.setAnswers(examAnswers);
        attempt.setScore(totalScore);
        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(ExamAttempt.ExamAttemptStatus.COMPLETED);

        if (totalScore >= exam.getPassingScore()) {
            attempt.setStatus(ExamAttempt.ExamAttemptStatus.PASSED);
            
            Enrollment enrollment = enrollmentRepository.findById(attempt.getEnrollmentId())
                    .orElseThrow(() -> new IllegalArgumentException("报名记录不存在"));
            enrollment.setStatus(Enrollment.EnrollmentStatus.CERTIFIED);
            enrollment.setCompletionDate(LocalDateTime.now());
            enrollmentRepository.save(enrollment);

            certificateService.createCertificate(enrollment, attempt);
        } else {
            attempt.setStatus(ExamAttempt.ExamAttemptStatus.FAILED);
            
            int attemptCount = attemptRepository.countAttemptsByEnrollmentId(attempt.getEnrollmentId());
            if (attemptCount > exam.getMaxRetakeAttempts()) {
                Enrollment enrollment = enrollmentRepository.findById(attempt.getEnrollmentId())
                        .orElseThrow(() -> new IllegalArgumentException("报名记录不存在"));
                enrollment.setStatus(Enrollment.EnrollmentStatus.FAILED);
                enrollmentRepository.save(enrollment);
            }
        }

        return attemptRepository.save(attempt);
    }

    public Optional<Exam> findByCourseId(Long courseId) {
        return examRepository.findByCourseId(courseId);
    }

    public Optional<ExamAttempt> findLatestAttemptByEnrollmentId(Long enrollmentId) {
        return attemptRepository.findLatestByEnrollmentId(enrollmentId);
    }

    public List<ExamAttempt> findAttemptsByEnrollmentId(Long enrollmentId) {
        return attemptRepository.findByEnrollmentId(enrollmentId);
    }

    public int getRemainingAttempts(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("报名记录不存在"));

        Exam exam = examRepository.findByCourseId(enrollment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("课程未配置考试"));

        int attemptCount = attemptRepository.countAttemptsByEnrollmentId(enrollmentId);
        int remaining = exam.getMaxRetakeAttempts() + 1 - attemptCount;

        return Math.max(0, remaining);
    }

    public Optional<ExamAttempt> findById(Long attemptId) {
        return attemptRepository.findById(attemptId);
    }
}
