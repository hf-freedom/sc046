package com.training.service;

import com.training.model.Certificate;
import com.training.model.Enrollment;
import com.training.model.LearningProgress;
import com.training.repository.EnrollmentRepository;
import com.training.repository.LearningProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LearningProgressRepository progressRepository;

    @Autowired
    private CertificateService certificateService;

    private static final int UNLEARNED_DAYS_THRESHOLD = 7;
    private static final int INACTIVITY_DAYS_THRESHOLD = 14;

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkInactiveLearners() {
        logger.info("开始扫描长期未学习的员工...");

        List<Enrollment> activeEnrollments = enrollmentRepository.findActiveEnrollments();
        LocalDateTime now = LocalDateTime.now();

        for (Enrollment enrollment : activeEnrollments) {
            List<LearningProgress> progressList = progressRepository.findByEnrollmentId(enrollment.getId());

            if (progressList.isEmpty()) {
                if (enrollment.getStartDate() == null) {
                    long daysSinceEnrollment = ChronoUnit.DAYS.between(
                            enrollment.getEnrollmentDate(),
                            now
                    );
                    if (daysSinceEnrollment >= UNLEARNED_DAYS_THRESHOLD) {
                        sendReminder(enrollment, "报名后" + daysSinceEnrollment + "天未开始学习");
                    }
                }
            } else {
                LocalDateTime lastActivity = progressList.stream()
                        .map(LearningProgress::getLastActivityDate)
                        .max(LocalDateTime::compareTo)
                        .orElse(enrollment.getStartDate());

                if (lastActivity != null) {
                    long daysSinceActivity = ChronoUnit.DAYS.between(lastActivity, now);
                    if (daysSinceActivity >= INACTIVITY_DAYS_THRESHOLD) {
                        sendReminder(enrollment, "已" + daysSinceActivity + "天未继续学习");
                    }
                }
            }
        }

        logger.info("扫描长期未学习的员工完成");
    }

    @Scheduled(cron = "0 0 10 * * ?")
    public void checkExpiringCertificates() {
        logger.info("开始检查即将到期的证书...");

        certificateService.updateExpiringSoonCertificates();
        certificateService.updateExpiredCertificates();

        List<Certificate> expiringSoon = certificateService.getCertificatesToRemind();
        for (Certificate cert : expiringSoon) {
            sendCertificateReminder(cert);
            certificateService.markReminderSent(cert.getId());
        }

        logger.info("检查即将到期的证书完成，共发送{}条提醒", expiringSoon.size());
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void dailyMaintenance() {
        logger.info("开始每日维护任务...");

        certificateService.updateExpiredCertificates();

        logger.info("每日维护任务完成");
    }

    private void sendReminder(Enrollment enrollment, String reason) {
        logger.info("发送学习提醒: 员工ID={}, 课程ID={}, 原因={}",
                enrollment.getEmployeeId(), enrollment.getCourseId(), reason);
    }

    private void sendCertificateReminder(Certificate certificate) {
        logger.info("发送证书到期提醒: 证书编号={}, 员工ID={}, 到期日期={}",
                certificate.getCertificateNo(), certificate.getEmployeeId(), certificate.getExpiryDate());
    }

    public void triggerInactiveLearnersCheck() {
        checkInactiveLearners();
    }

    public void triggerCertificateCheck() {
        checkExpiringCertificates();
    }
}
