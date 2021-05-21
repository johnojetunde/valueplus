package com.valueplus.persistence.entity.audit_mappers;

import com.valueplus.domain.enums.EntityType;
import com.valueplus.persistence.entity.ProductOrder;
import org.springframework.stereotype.Component;

@Component
public class ProductOrderEntityConverter implements AuditEntityToModel<ProductOrder> {
    @Override
    public Class<ProductOrder> clazz() {
        return ProductOrder.class;
    }

    @Override
    public EntityType entityType() {
        return EntityType.PRODUCT_ORDER;
    }
}