package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.model.WalletHistoryModel;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.entity.Wallet;
import com.codeemma.valueplus.persistence.entity.WalletHistory;
import com.codeemma.valueplus.persistence.repository.WalletHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.codeemma.valueplus.domain.enums.TransactionType.CREDIT;
import static com.codeemma.valueplus.domain.enums.TransactionType.DEBIT;
import static com.codeemma.valueplus.fixtures.TestFixtures.*;
import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class DefaultWalletHistoryServiceTest {
    @Mock
    private WalletHistoryRepository walletHistoryRepository;
    @InjectMocks
    private DefaultWalletHistoryService walletHistoryService;
    @Mock
    private Pageable pageable;
    private Wallet wallet;
    private Page<WalletHistory> pagedWalletHistory;

    @BeforeEach
    void setUp() {
        initMocks(this);
        User user = mockUser();
        wallet = getWallet(user);
        pagedWalletHistory = new PageImpl<>(singletonList(getWalletHistory(wallet, CREDIT)));
    }

    @Test
    void getHistory() {
        when(walletHistoryRepository.findWalletHistoriesByWallet_Id(eq(1L), eq(pageable)))
                .thenReturn(pagedWalletHistory);

        Page<WalletHistoryModel> history = walletHistoryService.getHistory(1L, pageable);

        assertThat(history).hasSize(1);
        WalletHistoryModel historyModel = history.getContent().get(0);
        assertThat(historyModel.getType()).isEqualTo(CREDIT);
        assertThat(historyModel.getWalletId()).isEqualTo(1L);
        assertThat(historyModel.getWalletHistoryId()).isEqualTo(1L);
        verify(walletHistoryRepository).findWalletHistoriesByWallet_Id(eq(1L), eq(pageable));
    }

    @Test
    void search_withUserId() throws ValuePlusException {
        when(walletHistoryRepository.findByUserIdAndDateBetween(
                eq(2L),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(pageable))).thenReturn(pagedWalletHistory);

        Page<WalletHistoryModel> history = walletHistoryService.search(2L, now(), now(), pageable);

        assertThat(history).hasSize(1);
        WalletHistoryModel historyModel = history.getContent().get(0);
        assertThat(historyModel.getType()).isEqualTo(CREDIT);
        assertThat(historyModel.getWalletId()).isEqualTo(1L);
        assertThat(historyModel.getWalletHistoryId()).isEqualTo(1L);
        verify(walletHistoryRepository).findByUserIdAndDateBetween(
                eq(2L),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(pageable));
    }

    @Test
    void search_withoutUserId() throws ValuePlusException {
        when(walletHistoryRepository.findByDateBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(pageable))).thenReturn(pagedWalletHistory);

        Page<WalletHistoryModel> history = walletHistoryService.search(null, now(), now(), pageable);

        assertThat(history).hasSize(1);
        WalletHistoryModel historyModel = history.getContent().get(0);
        assertThat(historyModel.getType()).isEqualTo(CREDIT);
        assertThat(historyModel.getWalletId()).isEqualTo(1L);
        assertThat(historyModel.getWalletHistoryId()).isEqualTo(1L);

        verify(walletHistoryRepository).findByDateBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(pageable));
    }

    @Test
    void createHistoryRecord() {
        when(walletHistoryRepository.save(any(WalletHistory.class)))
                .then(i -> {
                    var history = i.getArgument(0, WalletHistory.class);
                    history.setId(5L);
                    return history;
                });

        WalletHistoryModel history = walletHistoryService.createHistoryRecord(wallet, BigDecimal.TEN, DEBIT);

        assertThat(history.getType()).isEqualTo(DEBIT);
        assertThat(history.getWalletId()).isEqualTo(1L);
        assertThat(history.getWalletHistoryId()).isEqualTo(5L);
        verify(walletHistoryRepository).save(any(WalletHistory.class));
    }
}