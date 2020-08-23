package com.codeemma.valueplus.persistence.repository;

import com.codeemma.valueplus.persistence.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByUser_Id(Long userId, Pageable pageable, Sort sort);

    Page<Transaction> findAll(Pageable pageable, Sort sort);

    Optional<Transaction> findByUser_IdAndReference(Long userId, String reference);

    Page<Transaction> findByUser_IdAndCreatedAtIsBetween(Long userId,
                                                         LocalDateTime startDate,
                                                         LocalDateTime endDate,
                                                         Pageable pageable,
                                                         Sort sort);

    @Query("select t from Transaction t where t.status<>'success' OR t.status<>'error' OR t.status <> 'failed'")
    List<Transaction> findPendingTransactions();
}
