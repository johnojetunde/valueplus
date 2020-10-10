package com.codeemma.valueplus.persistence.repository;

import com.codeemma.valueplus.persistence.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findWalletByUser_Id(Long userId);

    Set<Wallet> findWalletsByUser_IdIn(List<Long> userIds);
}
