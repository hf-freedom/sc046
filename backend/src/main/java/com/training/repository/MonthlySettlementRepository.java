package com.training.repository;

import com.training.model.MonthlySettlement;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class MonthlySettlementRepository {

    private final Map<Long, MonthlySettlement> settlements = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public MonthlySettlement save(MonthlySettlement settlement) {
        if (settlement.getId() == null) {
            settlement.setId(idCounter.getAndIncrement());
            settlement.setCreatedAt(LocalDateTime.now());
        }
        settlement.setUpdatedAt(LocalDateTime.now());
        settlements.put(settlement.getId(), settlement);
        return settlement;
    }

    public Optional<MonthlySettlement> findById(Long id) {
        return Optional.ofNullable(settlements.get(id));
    }

    public Optional<MonthlySettlement> findByDepartmentIdAndMonth(Long departmentId, YearMonth month) {
        return settlements.values().stream()
                .filter(s -> departmentId.equals(s.getDepartmentId()))
                .filter(s -> month.equals(s.getSettlementMonth()))
                .findFirst();
    }

    public List<MonthlySettlement> findByDepartmentId(Long departmentId) {
        return settlements.values().stream()
                .filter(s -> departmentId.equals(s.getDepartmentId()))
                .collect(Collectors.toList());
    }

    public List<MonthlySettlement> findByMonth(YearMonth month) {
        return settlements.values().stream()
                .filter(s -> month.equals(s.getSettlementMonth()))
                .collect(Collectors.toList());
    }

    public List<MonthlySettlement> findByStatus(MonthlySettlement.SettlementStatus status) {
        return settlements.values().stream()
                .filter(s -> status.equals(s.getStatus()))
                .collect(Collectors.toList());
    }

    public List<MonthlySettlement> findAll() {
        return new ArrayList<>(settlements.values());
    }

    public void deleteById(Long id) {
        settlements.remove(id);
    }

    public boolean existsById(Long id) {
        return settlements.containsKey(id);
    }
}
