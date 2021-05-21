package com.valueplus.persistence.entity.audit_mappers;

import com.valueplus.domain.enums.EntityType;
import com.valueplus.persistence.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionEntityConverter implements AuditEntityToModel<Transaction> {
    @Override
    public Class<Transaction> clazz() {
        return Transaction.class;
    }

    @Override
    public EntityType entityType() {
        return EntityType.TRANSACTION;
    }
}
