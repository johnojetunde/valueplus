package com.codeemma.valueplus.persistence.repository;

import com.codeemma.valueplus.persistence.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByUser_IdOrderByIdDesc(Long userId, Pageable pageable);

    Page<Transaction> findAllByOrderByIdDesc(Pageable pageable);

    Optional<Transaction> findByUser_IdAndReference(Long userId, String reference);

    Optional<Transaction> findByReference(String reference);

    Page<Transaction> findByUser_IdAndCreatedAtIsBetweenOrderByIdDesc(Long userId,
                                                                      LocalDateTime startDate,
                                                                      LocalDateTime endDate,
                                                                      Pageable pageable);

    Page<Transaction> findByCreatedAtIsBetweenOrderByIdDesc(LocalDateTime startDate,
                                                            LocalDateTime endDate,
                                                            Pageable pageable);

    @Query("select t from Transaction t where t.status<>'success' OR t.status<>'error' OR t.status <> 'failed'")
    List<Transaction> findPendingTransactions();
}
