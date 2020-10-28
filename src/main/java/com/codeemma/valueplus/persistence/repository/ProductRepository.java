package com.codeemma.valueplus.persistence.repository;

import com.codeemma.valueplus.persistence.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findProductsByDisabledFalse(Pageable pageable);

    Optional<Product> findByNameAndIdIsNot(String name, Long id);

    Optional<Product> findByName(String name);

    Set<Product> findByIdIn(List<Long> productIds);
}
