package com.valueplus.domain.service.abstracts;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.enums.TransactionType;
import com.valueplus.domain.model.WalletHistoryModel;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface WalletHistoryService {
    Page<WalletHistoryModel> getHistory(User user, Long walletId, Pageable pageable) throws ValuePlusException;

    Page<WalletHistoryModel> getHistory(Pageable pageable) throws ValuePlusException;

    Page<WalletHistoryModel> search(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) throws ValuePlusException;

    Page<WalletHistoryModel> search(LocalDate startDate, LocalDate endDate, Pageable pageable) throws ValuePlusException;

    WalletHistoryModel createHistoryRecord(Wallet wallet, BigDecimal amount, TransactionType type, String description);
}
