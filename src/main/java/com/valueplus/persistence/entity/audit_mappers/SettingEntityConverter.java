package com.valueplus.persistence.entity.audit_mappers;

import com.valueplus.domain.enums.EntityType;
import com.valueplus.persistence.entity.Setting;
import org.springframework.stereotype.Component;

@Component
public class SettingEntityConverter implements AuditEntityToModel<Setting> {
    @Override
    public Class<Setting> clazz() {
        return Setting.class;
    }

    @Override
    public EntityType entityType() {
        return EntityType.SETTING;
    }
}
