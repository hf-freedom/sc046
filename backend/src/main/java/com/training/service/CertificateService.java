package com.training.service;

import com.training.model.*;
import com.training.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CourseRepository courseRepository;

    private static final int CERTIFICATE_VALIDITY_YEARS = 3;

    @Transactional
    public Certificate createCertificate(Enrollment enrollment, ExamAttempt examAttempt) {
        Optional<Certificate> existingCert = certificateRepository
                .findByEmployeeIdAndCourseId(enrollment.getEmployeeId(), enrollment.getCourseId());

        if (existingCert.isPresent()) {
            Certificate cert = existingCert.get();
            cert.setIssueDate(LocalDate.now());
            cert.setExpiryDate(LocalDate.now().plusYears(CERTIFICATE_VALIDITY_YEARS));
            cert.setStatus(Certificate.CertificateStatus.VALID);
            cert.setExamAttemptId(examAttempt.getId());
            cert.setReminderSent(false);
            return certificateRepository.save(cert);
        }

        Certificate certificate = Certificate.builder()
                .employeeId(enrollment.getEmployeeId())
                .courseId(enrollment.getCourseId())
                .examAttemptId(examAttempt.getId())
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(CERTIFICATE_VALIDITY_YEARS))
                .status(Certificate.CertificateStatus.VALID)
                .reminderSent(false)
                .build();

        return certificateRepository.save(certificate);
    }

    @Transactional
    public void revokeCertificate(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new IllegalArgumentException("证书不存在"));

        certificate.setStatus(Certificate.CertificateStatus.REVOKED);
        certificateRepository.save(certificate);
    }

    @Transactional
    public void markReminderSent(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new IllegalArgumentException("证书不存在"));

        certificate.setReminderSent(true);
        certificateRepository.save(certificate);
    }

    @Transactional
    public void updateExpiredCertificates() {
        List<Certificate> expiredCerts = certificateRepository.findExpired();
        for (Certificate cert : expiredCerts) {
            cert.setStatus(Certificate.CertificateStatus.EXPIRED);
            certificateRepository.save(cert);
        }
    }

    @Transactional
    public void updateExpiringSoonCertificates() {
        List<Certificate> expiringSoon = certificateRepository.findExpiringSoon();
        for (Certificate cert : expiringSoon) {
            if (Certificate.CertificateStatus.VALID.equals(cert.getStatus())) {
                cert.setStatus(Certificate.CertificateStatus.EXPIRING_SOON);
                certificateRepository.save(cert);
            }
        }
    }

    public List<Certificate> getCertificatesToRemind() {
        return certificateRepository.findExpiringSoon();
    }

    public Optional<Certificate> findById(Long id) {
        return certificateRepository.findById(id);
    }

    public Optional<Certificate> findByCertificateNo(String certificateNo) {
        return certificateRepository.findByCertificateNo(certificateNo);
    }

    public List<Certificate> findByEmployeeId(Long employeeId) {
        return certificateRepository.findByEmployeeId(employeeId);
    }

    public Optional<Certificate> findByEmployeeIdAndCourseId(Long employeeId, Long courseId) {
        return certificateRepository.findByEmployeeIdAndCourseId(employeeId, courseId);
    }

    public List<Certificate> findAll() {
        return certificateRepository.findAll();
    }
}
