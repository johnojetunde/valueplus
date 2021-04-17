package com.valueplus.app.config.audit;

import com.valueplus.domain.enums.ActionType;
import com.valueplus.domain.enums.EntityType;
import com.valueplus.domain.event.AuditEvent;
import com.valueplus.domain.model.AuditLogModel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditEventPublisher {
    private final ApplicationEventPublisher publisher;

    public void publish(Object currentData,
                        Object newData,
                        ActionType action,
                        EntityType entityType) {
        AuditLogModel auditLog = AuditLogModel.builder()
                .previousData(currentData)
                .newData(newData)
                .action(action)
                .entityType(entityType)
                .build();
        AuditEvent auditEvent = new AuditEvent(auditLog, this);
        this.publisher.publishEvent(auditEvent);
    }
}
