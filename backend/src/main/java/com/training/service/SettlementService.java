package com.training.service;

import com.training.model.*;
import com.training.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class SettlementService {

    @Autowired
    private MonthlySettlementRepository settlementRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Transactional
    public MonthlySettlement calculateMonthlySettlement(Long departmentId, YearMonth month) {
        Optional<MonthlySettlement> existing = settlementRepository
                .findByDepartmentIdAndMonth(departmentId, month);

        if (existing.isPresent()) {
            return existing.get();
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在"));

        List<Enrollment> departmentEnrollments = enrollmentRepository.findByDepartmentId(departmentId);

        BigDecimal totalEnrolledFee = BigDecimal.ZERO;
        BigDecimal totalCompletedFee = BigDecimal.ZERO;
        BigDecimal totalWithdrawnFee = BigDecimal.ZERO;

        for (Enrollment enrollment : departmentEnrollments) {
            YearMonth enrollmentMonth = YearMonth.from(enrollment.getEnrollmentDate());
            
            if (enrollmentMonth.equals(month)) {
                totalEnrolledFee = totalEnrolledFee.add(enrollment.getReservedFee());

                if (Enrollment.EnrollmentStatus.COMPLETED.equals(enrollment.getStatus())
                        || Enrollment.EnrollmentStatus.CERTIFIED.equals(enrollment.getStatus())) {
                    if (enrollment.getCompletionDate() != null &&
                        YearMonth.from(enrollment.getCompletionDate()).equals(month)) {
                        totalCompletedFee = totalCompletedFee.add(enrollment.getReservedFee());
                    }
                }

                if (Enrollment.EnrollmentStatus.WITHDRAWN.equals(enrollment.getStatus())) {
                    if (enrollment.getWithdrawalDate() != null &&
                        YearMonth.from(enrollment.getWithdrawalDate()).equals(month)) {
                        totalWithdrawnFee = totalWithdrawnFee.add(enrollment.getRefundAmount());
                    }
                }
            }
        }

        MonthlySettlement settlement = MonthlySettlement.builder()
                .departmentId(departmentId)
                .settlementMonth(month)
                .totalEnrolledFee(totalEnrolledFee)
                .totalCompletedFee(totalCompletedFee)
                .totalWithdrawnFee(totalWithdrawnFee)
                .netExpense(totalCompletedFee.subtract(totalWithdrawnFee))
                .status(MonthlySettlement.SettlementStatus.CALCULATED)
                .build();

        return settlementRepository.save(settlement);
    }

    @Transactional
    public MonthlySettlement approveSettlement(Long settlementId) {
        MonthlySettlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("结算记录不存在"));

        if (!MonthlySettlement.SettlementStatus.CALCULATED.equals(settlement.getStatus())) {
            throw new IllegalArgumentException("只有已计算的结算可以审批");
        }

        settlement.setStatus(MonthlySettlement.SettlementStatus.APPROVED);
        return settlementRepository.save(settlement);
    }

    @Transactional
    public MonthlySettlement settle(Long settlementId) {
        MonthlySettlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("结算记录不存在"));

        if (!MonthlySettlement.SettlementStatus.APPROVED.equals(settlement.getStatus())) {
            throw new IllegalArgumentException("只有已审批的结算可以执行");
        }

        departmentRepository.convertReservedToUsed(
                settlement.getDepartmentId(),
                settlement.getNetExpense()
        );

        settlement.setStatus(MonthlySettlement.SettlementStatus.SETTLED);
        return settlementRepository.save(settlement);
    }

    public Optional<MonthlySettlement> findById(Long id) {
        return settlementRepository.findById(id);
    }

    public Optional<MonthlySettlement> findByDepartmentIdAndMonth(Long departmentId, YearMonth month) {
        return settlementRepository.findByDepartmentIdAndMonth(departmentId, month);
    }

    public List<MonthlySettlement> findByDepartmentId(Long departmentId) {
        return settlementRepository.findByDepartmentId(departmentId);
    }

    public List<MonthlySettlement> findAll() {
        return settlementRepository.findAll();
    }
}
