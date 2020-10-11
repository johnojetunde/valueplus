package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.enums.TransactionType;
import com.codeemma.valueplus.domain.model.WalletHistoryModel;
import com.codeemma.valueplus.domain.model.WalletModel;
import com.codeemma.valueplus.domain.service.abstracts.WalletHistoryService;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.entity.Wallet;
import com.codeemma.valueplus.persistence.entity.WalletHistory;
import com.codeemma.valueplus.persistence.repository.WalletHistoryRepository;
import com.codeemma.valueplus.persistence.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.codeemma.valueplus.domain.model.RoleType.AGENT;
import static java.time.LocalTime.MAX;
import static java.time.LocalTime.MIN;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Slf4j
@RequiredArgsConstructor
@Service
public class DefaultWalletHistoryService implements WalletHistoryService {

    private final WalletHistoryRepository walletHistoryRepository;
    private final WalletRepository walletRepository;

    @Override
    public Page<WalletHistoryModel> getHistory(User user, Long walletId, Pageable pageable) throws ValuePlusException {
        WalletModel wallet = getWallet(user);

        if (isAgent(user) && !isWalletSame(walletId, wallet)) {
            throw new ValuePlusException("You can only get history of your wallet", FORBIDDEN);
        }

        return walletHistoryRepository.findWalletHistoriesByWallet_Id(walletId, pageable)
                .map(WalletHistory::toModel);
    }

    private WalletModel getWallet(User user) throws ValuePlusException {
        return walletRepository.findWalletByUser_Id(user.getId())
                .map(Wallet::toModel)
                .orElseThrow(() -> new ValuePlusException("Wallet does not exist", BAD_REQUEST));
    }

    private boolean isWalletSame(Long walletId, WalletModel wallet) {
        return wallet.getWalletId().equals(walletId);
    }

    private boolean isAgent(User user) {
        return AGENT.name().equals(user.getRole().getName());
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
    public WalletHistoryModel createHistoryRecord(Wallet wallet, BigDecimal amount, TransactionType type, String description) {
        var walletHistory = WalletHistory.builder()
                .amount(amount)
                .wallet(wallet)
                .description(description)
                .type(type)
                .build();

        return walletHistoryRepository.save(walletHistory).toModel();
    }

    private boolean userIdNotSet(Long userId) {
        return userId == null || userId == 0;
    }
}
