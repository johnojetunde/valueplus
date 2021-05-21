package com.valueplus.persistence.repository;

import com.valueplus.persistence.entity.TokenStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TokenStoreRepository extends JpaRepository<TokenStore, Long> {
    Optional<TokenStore> findByToken(String token);

    List<TokenStore> findTokenStoreByExpiredAtIsLessThanEqual(LocalDateTime date);
}
