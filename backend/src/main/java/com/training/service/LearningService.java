package com.training.service;

import com.training.model.*;
import com.training.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LearningService {

    @Autowired
    private LearningProgressRepository progressRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Transactional
    public LearningProgress startLearning(Long enrollmentId, Long chapterId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("报名记录不存在"));

        if (!Enrollment.EnrollmentStatus.ENROLLED.equals(enrollment.getStatus())
                && !Enrollment.EnrollmentStatus.IN_PROGRESS.equals(enrollment.getStatus())) {
            throw new IllegalArgumentException("报名状态不允许学习");
        }

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("章节不存在"));

        if (!chapter.getCourseId().equals(enrollment.getCourseId())) {
            throw new IllegalArgumentException("章节不属于该课程");
        }

        checkPrerequisites(enrollmentId, chapter);

        Optional<LearningProgress> existingProgress = progressRepository
                .findByEnrollmentIdAndChapterId(enrollmentId, chapterId);

        if (existingProgress.isPresent()) {
            LearningProgress progress = existingProgress.get();
            if (progress.isCompleted()) {
                throw new IllegalArgumentException("该章节已完成学习");
            }
            return progress;
        }

        if (enrollment.getStartDate() == null) {
            enrollment.setStartDate(LocalDateTime.now());
            enrollment.setStatus(Enrollment.EnrollmentStatus.IN_PROGRESS);
            enrollmentRepository.save(enrollment);
        }

        LearningProgress progress = LearningProgress.builder()
                .enrollmentId(enrollmentId)
                .employeeId(enrollment.getEmployeeId())
                .courseId(enrollment.getCourseId())
                .chapterId(chapterId)
                .studyDurationMinutes(0)
                .isCompleted(false)
                .startDate(LocalDateTime.now())
                .lastActivityDate(LocalDateTime.now())
                .build();

        return progressRepository.save(progress);
    }

    @Transactional
    public LearningProgress updateProgress(Long enrollmentId, Long chapterId, int additionalMinutes) {
        LearningProgress progress = progressRepository.findByEnrollmentIdAndChapterId(enrollmentId, chapterId)
                .orElseThrow(() -> new IllegalArgumentException("学习进度不存在"));

        if (progress.isCompleted()) {
            throw new IllegalArgumentException("该章节已完成学习");
        }

        progress.setStudyDurationMinutes(progress.getStudyDurationMinutes() + additionalMinutes);
        progress.setLastActivityDate(LocalDateTime.now());

        Chapter chapter = chapterRepository.findById(chapterId).orElse(null);
        if (chapter != null && progress.meetsMinDuration(chapter.getMinStudyDurationMinutes())) {
            progress.setCompleted(true);
            progress.setCompletionDate(LocalDateTime.now());
            checkCourseCompletion(enrollmentId);
        }

        return progressRepository.save(progress);
    }

    @Transactional
    public LearningProgress completeChapter(Long enrollmentId, Long chapterId) {
        LearningProgress progress = progressRepository.findByEnrollmentIdAndChapterId(enrollmentId, chapterId)
                .orElseThrow(() -> new IllegalArgumentException("学习进度不存在"));

        if (progress.isCompleted()) {
            return progress;
        }

        Chapter chapter = chapterRepository.findById(chapterId).orElse(null);
        if (chapter != null && !progress.meetsMinDuration(chapter.getMinStudyDurationMinutes())) {
            throw new IllegalArgumentException("未达到最低学习时长要求");
        }

        progress.setCompleted(true);
        progress.setCompletionDate(LocalDateTime.now());
        progress.setLastActivityDate(LocalDateTime.now());

        checkCourseCompletion(enrollmentId);

        return progressRepository.save(progress);
    }

    private void checkPrerequisites(Long enrollmentId, Chapter chapter) {
        if (chapter.getPrerequisiteChapterIds() == null || chapter.getPrerequisiteChapterIds().isEmpty()) {
            return;
        }

        for (Long prereqId : chapter.getPrerequisiteChapterIds()) {
            Optional<LearningProgress> prereqProgress = progressRepository
                    .findByEnrollmentIdAndChapterId(enrollmentId, prereqId);

            if (!prereqProgress.isPresent() || !prereqProgress.get().isCompleted()) {
                Chapter prereqChapter = chapterRepository.findById(prereqId).orElse(null);
                String chapterName = prereqChapter != null ? prereqChapter.getTitle() : "前置章节";
                throw new IllegalArgumentException("需要先完成: " + chapterName);
            }
        }
    }

    private void checkCourseCompletion(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
        if (enrollment == null) return;

        List<Chapter> mandatoryChapters = chapterRepository
                .findMandatoryChaptersByCourseId(enrollment.getCourseId());

        List<LearningProgress> completedProgress = progressRepository
                .findCompletedByEnrollmentId(enrollmentId);

        boolean allMandatoryCompleted = mandatoryChapters.stream()
                .allMatch(chapter -> completedProgress.stream()
                        .anyMatch(p -> p.getChapterId().equals(chapter.getId())));

        if (allMandatoryCompleted && mandatoryChapters.size() > 0) {
            enrollment.setStatus(Enrollment.EnrollmentStatus.COMPLETED);
            enrollmentRepository.save(enrollment);
        }
    }

    public boolean canTakeExam(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
        if (enrollment == null) return false;

        if (!Enrollment.EnrollmentStatus.COMPLETED.equals(enrollment.getStatus())
                && !Enrollment.EnrollmentStatus.IN_PROGRESS.equals(enrollment.getStatus())) {
            return false;
        }

        List<Chapter> mandatoryChapters = chapterRepository
                .findMandatoryChaptersByCourseId(enrollment.getCourseId());

        List<LearningProgress> completedProgress = progressRepository
                .findCompletedByEnrollmentId(enrollmentId);

        return mandatoryChapters.stream()
                .allMatch(chapter -> completedProgress.stream()
                        .anyMatch(p -> p.getChapterId().equals(chapter.getId())));
    }

    public Optional<LearningProgress> findByEnrollmentIdAndChapterId(Long enrollmentId, Long chapterId) {
        return progressRepository.findByEnrollmentIdAndChapterId(enrollmentId, chapterId);
    }

    public List<LearningProgress> findByEnrollmentId(Long enrollmentId) {
        return progressRepository.findByEnrollmentId(enrollmentId);
    }

    public List<LearningProgress> findByEmployeeIdAndCourseId(Long employeeId, Long courseId) {
        return progressRepository.findByEmployeeIdAndCourseId(employeeId, courseId);
    }
}
