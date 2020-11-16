package com.valueplus.domain.service.concretes;

import com.valueplus.persistence.entity.TokenStore;
import com.valueplus.persistence.repository.TokenStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.runAsync;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenStoreRepository tokenStoreRepository;

    public CompletableFuture<Void> deleteExpiredTokens() {
        return runAsync(() -> {
            List<TokenStore> expiredTokens = tokenStoreRepository.findTokenStoreByExpiredAtIsLessThanEqual(LocalDateTime.now());
            tokenStoreRepository.deleteAll(expiredTokens);
        });
    }
}
