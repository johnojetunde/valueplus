package com.valueplus.persistence.repository;

import com.valueplus.persistence.entity.WalletHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface WalletHistoryRepository extends JpaRepository<WalletHistory, Long> {

    Page<WalletHistory> findWalletHistoriesByWallet_Id(Long walletId, Pageable pageable);


    @Query("SELECT wh from WalletHistory wh WHERE " +
            "wh.wallet.user.id = :userId AND wh.createdAt >= :startDate AND wh.createdAt <= :endDate")
    Page<WalletHistory> findByUserIdAndDateBetween(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDateTime starDate,
                                                   @Param("endDate") LocalDateTime endDate,
                                                   Pageable pageable);

    @Query("SELECT wh from WalletHistory wh WHERE wh.createdAt >= :startDate AND wh.createdAt <= :endDate")
    Page<WalletHistory> findByDateBetween(@Param("startDate") LocalDateTime starDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);
}
