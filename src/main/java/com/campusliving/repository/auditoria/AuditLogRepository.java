package com.campusliving.repository.auditoria;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.auditoria.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
