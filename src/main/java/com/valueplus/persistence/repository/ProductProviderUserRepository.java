package com.valueplus.persistence.repository;

import com.valueplus.persistence.entity.ProductProviderUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductProviderUserRepository extends JpaRepository<ProductProviderUser, Long> {
}
