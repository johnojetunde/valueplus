package com.codeemma.valueplus.persistence.repository;

import com.codeemma.valueplus.persistence.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByUser_Id(Long userId, Pageable pageable);
}
