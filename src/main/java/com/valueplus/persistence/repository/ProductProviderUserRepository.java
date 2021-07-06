package com.valueplus.persistence.repository;

import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.persistence.entity.ProductProviderUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductProviderUserRepository extends JpaRepository<ProductProviderUser, Long> {
    Optional<ProductProviderUser> findByAgentCodeAndProvider(String agentCode, ProductProvider provider);
}
