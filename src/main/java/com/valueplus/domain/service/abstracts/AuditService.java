package com.valueplus.domain.service.abstracts;

import com.valueplus.domain.model.AuditLogModel;
import com.valueplus.domain.model.AuditModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    void save(AuditLogModel model) throws Exception;

    Page<AuditLogModel> query(AuditModel model, Pageable pageable) throws Exception;
}
