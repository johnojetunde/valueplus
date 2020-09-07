package com.codeemma.valueplus.persistence.repository;

import com.codeemma.valueplus.persistence.entity.ProductOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long>, JpaSpecificationExecutor<ProductOrder> {

    Page<ProductOrder> findByProduct_id(Long productId, Pageable pageable);

    Page<ProductOrder> findByUser_idAndProduct_id(Long userId, Long productId, Pageable pageable);

    Page<ProductOrder> findByUser_id(Long userid, Pageable pageable);

    Optional<ProductOrder> findByIdAndUser_id(Long id, Long userId);
}
