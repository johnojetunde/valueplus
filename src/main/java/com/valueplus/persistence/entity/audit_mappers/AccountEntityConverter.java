package com.valueplus.persistence.entity.audit_mappers;

import com.valueplus.domain.enums.EntityType;
import com.valueplus.persistence.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountEntityConverter implements AuditEntityToModel<Account> {
    @Override
    public Class<Account> clazz() {
        return Account.class;
    }

    @Override
    public EntityType entityType() {
        return EntityType.ACCOUNT;
    }
}
