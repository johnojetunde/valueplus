package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.enums.TransactionType;
import com.codeemma.valueplus.domain.model.WalletHistoryModel;
import com.codeemma.valueplus.domain.service.abstracts.WalletHistoryService;
import com.codeemma.valueplus.persistence.entity.Wallet;
import com.codeemma.valueplus.persistence.entity.WalletHistory;
import com.codeemma.valueplus.persistence.repository.WalletHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.LocalTime.MAX;
import static java.time.LocalTime.MIN;

@Slf4j
@RequiredArgsConstructor
@Service
public class DefaultWalletHistoryService implements WalletHistoryService {

    private final WalletHistoryRepository walletHistoryRepository;

    @Override
    public Page<WalletHistoryModel> getHistory(Long walletId, Pageable pageable) {
        return walletHistoryRepository.findWalletHistoriesByWallet_Id(walletId, pageable)
                .map(WalletHistory::toModel);
    }

    @Override
    public Page<WalletHistoryModel> search(Long userId,
                                           LocalDate startDate,
                                           LocalDate endDate,
                                           Pageable pageable) throws ValuePlusException {
        try {
            LocalDateTime startDateTime = LocalDateTime.of(startDate, MIN);
            LocalDateTime endDateTime = LocalDateTime.of(endDate, MAX);

            return userIdNotSet(userId)
                    ? walletHistoryRepository.findByDateBetween(startDateTime, endDateTime, pageable)
                    .map(WalletHistory::toModel)
                    : walletHistoryRepository.findByUserIdAndDateBetween(userId, startDateTime, endDateTime, pageable)
                    .map(WalletHistory::toModel);

        } catch (Exception e) {
            log.error("Error searching for history", e);
            throw new ValuePlusException("Error searching for history", e);
        }

    }

    @Override
    public WalletHistoryModel createHistoryRecord(Wallet wallet, BigDecimal amount, TransactionType type) {
        var walletHistory = WalletHistory.builder()
                .amount(amount)
                .wallet(wallet)
                .type(type)
                .build();

        return walletHistoryRepository.save(walletHistory).toModel();
    }

    private boolean userIdNotSet(Long userId) {
        return userId == null || userId == 0;
    }
}
