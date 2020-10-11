package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.model.WalletHistoryModel;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.entity.Wallet;
import com.codeemma.valueplus.persistence.entity.WalletHistory;
import com.codeemma.valueplus.persistence.repository.WalletHistoryRepository;
import com.codeemma.valueplus.persistence.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.codeemma.valueplus.domain.enums.TransactionType.CREDIT;
import static com.codeemma.valueplus.domain.enums.TransactionType.DEBIT;
import static com.codeemma.valueplus.fixtures.TestFixtures.*;
import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.FORBIDDEN;

class DefaultWalletHistoryServiceTest {
    @Mock
    private WalletHistoryRepository walletHistoryRepository;
    @Mock
    private WalletRepository walletRepository;
    @InjectMocks
    private DefaultWalletHistoryService walletHistoryService;
    @Mock
    private Pageable pageable;
    private Wallet wallet;
    private User user;
    private Page<WalletHistory> pagedWalletHistory;

    @BeforeEach
    void setUp() {
        initMocks(this);
        user = mockUser();
        wallet = getWallet(user);
        pagedWalletHistory = new PageImpl<>(singletonList(getWalletHistory(wallet, CREDIT)));
    }

    @Test
    void getHistory() throws ValuePlusException {
        when(walletHistoryRepository.findWalletHistoriesByWallet_Id(eq(1L), eq(pageable)))
                .thenReturn(pagedWalletHistory);
        when(walletRepository.findWalletByUser_Id(eq(1L)))
                .thenReturn(Optional.of(wallet));

        Page<WalletHistoryModel> history = walletHistoryService.getHistory(user, 1L, pageable);

        assertThat(history).hasSize(1);
        WalletHistoryModel historyModel = history.getContent().get(0);
        assertThat(historyModel.getType()).isEqualTo(CREDIT);
        assertThat(historyModel.getWalletId()).isEqualTo(1L);
        assertThat(historyModel.getWalletHistoryId()).isEqualTo(1L);
        verify(walletHistoryRepository).findWalletHistoriesByWallet_Id(eq(1L), eq(pageable));
        verify(walletRepository).findWalletByUser_Id(eq(1L));
    }

    @Test
    void getHistory_Forbidden() throws ValuePlusException {
        when(walletRepository.findWalletByUser_Id(eq(1L)))
                .thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletHistoryService.getHistory(user, 2L, pageable))
                .isInstanceOf(ValuePlusException.class)
                .hasFieldOrPropertyWithValue("message", "You can only get history of your wallet")
                .hasFieldOrPropertyWithValue("httpStatus", FORBIDDEN);

        verify(walletRepository).findWalletByUser_Id(eq(1L));
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

        WalletHistoryModel history = walletHistoryService.createHistoryRecord(wallet, BigDecimal.TEN, DEBIT, "Description");

        assertThat(history.getType()).isEqualTo(DEBIT);
        assertThat(history.getWalletId()).isEqualTo(1L);
        assertThat(history.getWalletHistoryId()).isEqualTo(5L);
        assertThat(history.getDescription()).isEqualTo("Description");
        verify(walletHistoryRepository).save(any(WalletHistory.class));
    }
}