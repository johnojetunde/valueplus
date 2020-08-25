package com.codeemma.valueplus.persistence.repository;

import com.codeemma.valueplus.persistence.entity.ProductOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long>, JpaSpecificationExecutor<ProductOrder> {

    Page<ProductOrder> findByProduct_id(Long productId, Pageable pageable);
}
