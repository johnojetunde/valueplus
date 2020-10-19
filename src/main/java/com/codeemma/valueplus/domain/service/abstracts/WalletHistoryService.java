package com.codeemma.valueplus.domain.service.abstracts;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.enums.TransactionType;
import com.codeemma.valueplus.domain.model.WalletHistoryModel;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.entity.Wallet;
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
