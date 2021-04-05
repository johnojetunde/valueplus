package com.valueplus.persistence.repository;

import com.valueplus.domain.enums.ActionType;
import com.valueplus.domain.enums.EntityType;
import com.valueplus.persistence.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    Page<AuditLog> findByEntityTypeAndCreatedAtBetween(EntityType type,
                                                       LocalDateTime startDate,
                                                       LocalDateTime endDate,
                                                       Pageable pageable);

    Page<AuditLog> findByEntityTypeAndActionTypeAndCreatedAtBetween(EntityType type,
                                                                    ActionType action,
                                                                    LocalDateTime startDate,
                                                                    LocalDateTime endDate,
                                                                    Pageable pageable);
}
