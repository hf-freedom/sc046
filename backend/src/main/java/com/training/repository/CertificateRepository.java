package com.training.repository;

import com.training.model.Certificate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class CertificateRepository {

    private final Map<Long, Certificate> certificates = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Certificate save(Certificate certificate) {
        if (certificate.getId() == null) {
            certificate.setId(idCounter.getAndIncrement());
            certificate.setCreatedAt(LocalDateTime.now());
            if (certificate.getCertificateNo() == null) {
                certificate.setCertificateNo(generateCertificateNo());
            }
        }
        certificate.setUpdatedAt(LocalDateTime.now());
        certificates.put(certificate.getId(), certificate);
        return certificate;
    }

    public Optional<Certificate> findById(Long id) {
        return Optional.ofNullable(certificates.get(id));
    }

    public Optional<Certificate> findByCertificateNo(String certificateNo) {
        return certificates.values().stream()
                .filter(c -> certificateNo.equals(c.getCertificateNo()))
                .findFirst();
    }

    public List<Certificate> findByEmployeeId(Long employeeId) {
        return certificates.values().stream()
                .filter(c -> employeeId.equals(c.getEmployeeId()))
                .collect(Collectors.toList());
    }

    public Optional<Certificate> findByEmployeeIdAndCourseId(Long employeeId, Long courseId) {
        return certificates.values().stream()
                .filter(c -> employeeId.equals(c.getEmployeeId()))
                .filter(c -> courseId.equals(c.getCourseId()))
                .filter(c -> Certificate.CertificateStatus.VALID.equals(c.getStatus())
                        || Certificate.CertificateStatus.EXPIRING_SOON.equals(c.getStatus()))
                .findFirst();
    }

    public List<Certificate> findExpiringSoon() {
        LocalDate now = LocalDate.now();
        LocalDate oneMonthLater = now.plusMonths(1);
        return certificates.values().stream()
                .filter(c -> Certificate.CertificateStatus.VALID.equals(c.getStatus()))
                .filter(c -> c.getExpiryDate() != null)
                .filter(c -> c.getExpiryDate().isAfter(now) && c.getExpiryDate().isBefore(oneMonthLater.plusDays(1)))
                .filter(c -> !c.isReminderSent())
                .collect(Collectors.toList());
    }

    public List<Certificate> findExpired() {
        LocalDate now = LocalDate.now();
        return certificates.values().stream()
                .filter(c -> Certificate.CertificateStatus.VALID.equals(c.getStatus())
                        || Certificate.CertificateStatus.EXPIRING_SOON.equals(c.getStatus()))
                .filter(c -> c.getExpiryDate() != null)
                .filter(c -> c.getExpiryDate().isBefore(now))
                .collect(Collectors.toList());
    }

    public List<Certificate> findAll() {
        return new ArrayList<>(certificates.values());
    }

    public void deleteById(Long id) {
        certificates.remove(id);
    }

    public boolean existsById(Long id) {
        return certificates.containsKey(id);
    }

    private String generateCertificateNo() {
        return "CERT-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
