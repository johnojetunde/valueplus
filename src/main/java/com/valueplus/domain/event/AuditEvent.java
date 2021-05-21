package com.valueplus.domain.event;

import com.valueplus.domain.model.AuditLogModel;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AuditEvent extends ApplicationEvent {
    private final AuditLogModel auditLog;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public AuditEvent(AuditLogModel auditLog, Object source) {
        super(source);
        this.auditLog = auditLog;
    }
}
