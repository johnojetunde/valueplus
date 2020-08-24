package com.codeemma.valueplus.persistence.repository;

import com.codeemma.valueplus.persistence.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByNameAndIdIsNot(String name, Long id);

    Optional<Product> findByName(String name);
}
