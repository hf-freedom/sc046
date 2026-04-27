package com.training.controller;

import com.training.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduler")
@CrossOrigin(origins = "http://localhost:3003")
public class SchedulerController {

    @Autowired
    private SchedulerService schedulerService;

    @PostMapping("/check-inactive-learners")
    public String checkInactiveLearners() {
        schedulerService.triggerInactiveLearnersCheck();
        return "已触发长期未学习员工检查";
    }

    @PostMapping("/check-expiring-certificates")
    public String checkExpiringCertificates() {
        schedulerService.triggerCertificateCheck();
        return "已触发证书到期检查";
    }
}
