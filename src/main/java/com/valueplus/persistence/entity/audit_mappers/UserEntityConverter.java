package com.valueplus.persistence.entity.audit_mappers;

import com.valueplus.domain.enums.EntityType;
import com.valueplus.persistence.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserEntityConverter implements AuditEntityToModel<User> {
    @Override
    public Class<User> clazz() {
        return User.class;
    }

    @Override
    public EntityType entityType() {
        return EntityType.USER;
    }
}
