package com.smartbooking.service;

import com.smartbooking.domain.AuditLog;
import com.smartbooking.persistence.AuditLogRepository;

import java.time.LocalDateTime;
import java.util.List;

public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(long userId, String action, String details) {
        auditLogRepository.create(new AuditLog(0L, userId, action, details, LocalDateTime.now()));
    }

    public List<AuditLog> listLogs() {
        return auditLogRepository.findAll();
    }
}
