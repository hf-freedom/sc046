package com.training.controller;

import com.training.model.MonthlySettlement;
import com.training.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@CrossOrigin(origins = "http://localhost:3003")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    @GetMapping
    public List<MonthlySettlement> getAllSettlements() {
        return settlementService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonthlySettlement> getSettlementById(@PathVariable Long id) {
        return settlementService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/department/{departmentId}")
    public List<MonthlySettlement> getSettlementsByDepartment(@PathVariable Long departmentId) {
        return settlementService.findByDepartmentId(departmentId);
    }

    @GetMapping("/department/{departmentId}/month/{yearMonth}")
    public ResponseEntity<MonthlySettlement> getSettlementByDepartmentAndMonth(
            @PathVariable Long departmentId,
            @PathVariable String yearMonth) {
        YearMonth month = YearMonth.parse(yearMonth);
        return settlementService.findByDepartmentIdAndMonth(departmentId, month)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/calculate")
    public ResponseEntity<?> calculateSettlement(
            @RequestParam Long departmentId,
            @RequestParam String yearMonth) {
        try {
            YearMonth month = YearMonth.parse(yearMonth);
            MonthlySettlement settlement = settlementService.calculateMonthlySettlement(departmentId, month);
            return ResponseEntity.ok(settlement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveSettlement(@PathVariable Long id) {
        try {
            MonthlySettlement settlement = settlementService.approveSettlement(id);
            return ResponseEntity.ok(settlement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/settle")
    public ResponseEntity<?> settle(@PathVariable Long id) {
        try {
            MonthlySettlement settlement = settlementService.settle(id);
            return ResponseEntity.ok(settlement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
