package com.valueplus.persistence.entity.audit_mappers;

import com.valueplus.domain.enums.EntityType;
import com.valueplus.persistence.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductEntityConverter implements AuditEntityToModel<Product> {
    @Override
    public Class<Product> clazz() {
        return Product.class;
    }

    @Override
    public EntityType entityType() {
        return EntityType.PRODUCT;
    }
}
