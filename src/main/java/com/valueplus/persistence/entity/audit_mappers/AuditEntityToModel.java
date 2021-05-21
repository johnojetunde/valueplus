package com.valueplus.persistence.entity.audit_mappers;

import com.valueplus.domain.enums.EntityType;
import com.valueplus.persistence.entity.ToModel;

import java.util.Optional;

import static com.valueplus.domain.util.MapperUtil.MAPPER;

public interface AuditEntityToModel<T extends ToModel> {
    default Optional<Object> toModel(String entityObject) {
        try {
            T entity = MAPPER.readValue(entityObject, clazz());
            return Optional.of(entity.toModel());
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    Class<T> clazz();

    EntityType entityType();
}
