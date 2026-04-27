package com.training.controller;

import com.training.model.Certificate;
import com.training.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = "http://localhost:3003")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @GetMapping
    public List<Certificate> getAllCertificates() {
        return certificateService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Certificate> getCertificateById(@PathVariable Long id) {
        return certificateService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{certificateNo}")
    public ResponseEntity<Certificate> getCertificateByNo(@PathVariable String certificateNo) {
        return certificateService.findByCertificateNo(certificateNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public List<Certificate> getCertificatesByEmployee(@PathVariable Long employeeId) {
        return certificateService.findByEmployeeId(employeeId);
    }

    @GetMapping("/employee/{employeeId}/course/{courseId}")
    public ResponseEntity<Certificate> getCertificateByEmployeeAndCourse(
            @PathVariable Long employeeId,
            @PathVariable Long courseId) {
        return certificateService.findByEmployeeIdAndCourseId(employeeId, courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<?> revokeCertificate(@PathVariable Long id) {
        try {
            certificateService.revokeCertificate(id);
            return ResponseEntity.ok("证书已吊销");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/check-expired")
    public String checkExpiredCertificates() {
        certificateService.updateExpiredCertificates();
        return "已更新过期证书状态";
    }

    @PostMapping("/check-expiring")
    public String checkExpiringCertificates() {
        certificateService.updateExpiringSoonCertificates();
        return "已检查即将到期的证书";
    }
}
