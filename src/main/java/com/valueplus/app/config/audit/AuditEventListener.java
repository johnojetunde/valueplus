package com.valueplus.app.config.audit;

import com.valueplus.domain.event.AuditEvent;
import com.valueplus.domain.service.abstracts.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuditEventListener implements ApplicationListener<AuditEvent> {
    private final AuditService auditService;

    @SneakyThrows
    @Async
    @Override
    public void onApplicationEvent(AuditEvent event) {
        auditService.save(event.getAuditLog());
    }
}
