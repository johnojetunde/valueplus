package com.valueplus.persistence.entity.audit_mappers;

import com.valueplus.domain.enums.EntityType;
import com.valueplus.persistence.entity.ToModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;

@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
@Service
public class AuditEntityConverterService {
    private final List<AuditEntityToModel> modelConverters;
    private final AuditEntityToModel DEFAULT_CONVERTER = new DefaultEntityConverter();

    public Object toObject(String entity, EntityType entityType) {
        AuditEntityToModel mapper = emptyIfNullStream(modelConverters)
                .filter(converter -> entityType.equals(converter.entityType()))
                .findFirst()
                .orElse(DEFAULT_CONVERTER);

        return mapper.toModel(entity);
    }

    private class DefaultEntityConverter implements AuditEntityToModel<ToModel> {
        @Override
        public Optional<Object> toModel(String entityObject) {
            return Optional.of(entityObject);
        }

        @Override
        public Class<ToModel> clazz() {
            return ToModel.class;
        }

        @Override
        public EntityType entityType() {
            return null;
        }
    }
}
