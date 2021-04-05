package com.valueplus.domain.service.concretes;

import com.valueplus.app.config.audit.AuditEventPublisher;
import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.enums.ActionType;
import com.valueplus.domain.enums.EntityType;
import com.valueplus.domain.model.AccountModel;
import com.valueplus.domain.model.SettingModel;
import com.valueplus.domain.model.TransactionModel;
import com.valueplus.domain.model.WalletModel;
import com.valueplus.domain.service.abstracts.PaymentService;
import com.valueplus.domain.service.abstracts.SettingsService;
import com.valueplus.domain.service.abstracts.WalletService;
import com.valueplus.fixtures.TestFixtures;
import com.valueplus.persistence.entity.Role;
import com.valueplus.persistence.entity.Transaction;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.AccountRepository;
import com.valueplus.persistence.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.valueplus.domain.util.FunctionUtil.setScale;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

class DefaultTransferServiceTest {

    @Mock
    private PaymentService paymentService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private Pageable pageable;
    @Mock
    private WalletService walletService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SettingsService settingsService;
    @Mock
    private AuditEventPublisher auditEvent;

    private DefaultTransferService transferService;

    private User agentUser;
    private User adminUser;
    private Transaction transaction;
    private Page<Transaction> pagedTransaction;

    @BeforeEach
    void setUp() throws ValuePlusException {
        String accountNumber = "0011313333";
        initMocks(this);

        transferService = new DefaultTransferService(
                paymentService,
                transactionRepository,
                accountRepository,
                walletService,
                passwordEncoder,
                settingsService,
                auditEvent);
        agentUser = TestFixtures.mockUser();
        adminUser = TestFixtures.mockUser();
        adminUser.setRole(new Role("ADMIN"));

        transaction = TestFixtures.mockTransaction(accountNumber);
        pagedTransaction = new PageImpl<>(singletonList(transaction));
        when(accountRepository.findByUser_Id(anyLong()))
                .thenReturn(Optional.of(TestFixtures.mockAccount(accountNumber)));
        when(paymentService.transfer(any(AccountModel.class), any(BigDecimal.class)))
                .thenReturn(TestFixtures.mockTransferResponse(BigDecimal.valueOf(100)));
        when(transactionRepository.save(any(Transaction.class)))
                .then(i -> i.getArgument(0, Transaction.class));
        when(passwordEncoder.matches(isA(CharSequence.class), isA(String.class)))
                .thenReturn(true);
        var settings = SettingModel.builder()
                .commissionPercentage(BigDecimal.valueOf(15))
                .build();
        when(settingsService.getCurrentSetting()).thenReturn(Optional.of(settings));
        doNothing().when(auditEvent)
                .publish(isA(Transaction.class), isA(Transaction.class), isA(ActionType.class), isA(EntityType.class));
    }

    @Test
    void transferFailsWhenPinIsNotSet() {
        var requestModel = TestFixtures.mockPaymentRequestModel(BigDecimal.TEN, "1234");
        agentUser.setTransactionPin(null);

        assertThatThrownBy(() -> transferService.transfer(agentUser, requestModel))
                .isInstanceOf(ValuePlusException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.BAD_REQUEST)
                .hasFieldOrPropertyWithValue("message", "You need to set your transaction pin");
    }

    @Test
    void transferFailsWhenPinDoesNotMatch() {
        when(passwordEncoder.matches(isA(CharSequence.class), isA(String.class)))
                .thenReturn(false);

        var requestModel = TestFixtures.mockPaymentRequestModel(BigDecimal.TEN, "1122");

        assertThatThrownBy(() -> transferService.transfer(agentUser, requestModel))
                .isInstanceOf(ValuePlusException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.UNAUTHORIZED)
                .hasFieldOrPropertyWithValue("message", "Incorrect pin");
    }

    @Test
    void transferFailWhenSettingDoesNotExist() throws ValuePlusException {
        when(settingsService.getCurrentSetting()).thenReturn(Optional.empty());
        var requestModel = TestFixtures.mockPaymentRequestModel(BigDecimal.TEN, "1234");
        var expectedActualTransfer = setScale(BigDecimal.valueOf(8.50));
        var debitAmount = setScale(requestModel.getAmount());
        var commission = setScale(BigDecimal.valueOf(1.50));

        when(walletService.debitWallet(eq(agentUser), eq(debitAmount), anyString()))
                .thenReturn(WalletModel.builder().build());
        when(walletService.creditAdminWallet(eq(commission), anyString()))
                .thenReturn(WalletModel.builder().build());

        assertThatThrownBy(() -> transferService.transfer(agentUser, requestModel))
                .isInstanceOf(ValuePlusException.class)
                .hasFieldOrPropertyWithValue("httpStatus", INTERNAL_SERVER_ERROR)
                .hasFieldOrPropertyWithValue("message", "Unable to retrieve commission");

        verify(accountRepository).findByUser_Id(anyLong());
    }

    @Test
    void transfer() throws ValuePlusException {
        var requestModel = TestFixtures.mockPaymentRequestModel(BigDecimal.TEN, "1234");
        var expectedActualTransfer = setScale(BigDecimal.valueOf(8.50));
        var debitAmount = setScale(requestModel.getAmount());
        var commission = setScale(BigDecimal.valueOf(1.50));

        when(walletService.debitWallet(eq(agentUser), eq(debitAmount), anyString()))
                .thenReturn(WalletModel.builder().build());
        when(walletService.creditAdminWallet(eq(commission), anyString()))
                .thenReturn(WalletModel.builder().build());

        TransactionModel model = transferService.transfer(agentUser, requestModel);

        verify(accountRepository).findByUser_Id(anyLong());
        verify(paymentService).transfer(any(AccountModel.class), eq(expectedActualTransfer));
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletService).debitWallet(eq(agentUser), eq(debitAmount), anyString());
        verify(walletService).creditAdminWallet(eq(commission), anyString());
    }

    @Test
    void getAllUserTransactions() throws ValuePlusException {
        when(transactionRepository.findByUser_IdOrderByIdDesc(anyLong(), eq(pageable)))
                .thenReturn(pagedTransaction);

        var result = transferService.getAllUserTransactions(agentUser, pageable);

        var transaction = result.getContent().get(0);

        assertPagedResult(result);
        assertTransaction(transaction);

        verify(transactionRepository).findByUser_IdOrderByIdDesc(anyLong(), eq(pageable));
    }

    @Test
    void getAllTransactions() throws ValuePlusException {
        when(transactionRepository.findAllByOrderByIdDesc(eq(pageable)))
                .thenReturn(pagedTransaction);

        var result = transferService.getAllTransactions(pageable);

        var transaction = result.getContent().get(0);

        assertPagedResult(result);
        assertTransaction(transaction);

        verify(transactionRepository).findAllByOrderByIdDesc(eq(pageable));
    }

    @Test
    void getTransactionByReference() throws ValuePlusException {
        when(transactionRepository.findByUser_IdAndReference(anyLong(), anyString()))
                .thenReturn(Optional.of(transaction));

        var result = transferService.getTransactionByReference(agentUser, "reference");

        assertTrue(result.isPresent());
        assertTransaction(result.get());

        verify(transactionRepository).findByUser_IdAndReference(anyLong(), anyString());
    }

    @Test
    void getTransactionByReference_Admin() throws ValuePlusException {
        when(transactionRepository.findByReference(anyString()))
                .thenReturn(Optional.of(transaction));

        var result = transferService.getTransactionByReference(adminUser, "reference");

        assertTrue(result.isPresent());
        assertTransaction(result.get());

        verify(transactionRepository).findByReference(anyString());
    }

    @Test
    void getTransactionByReference_returnsEmpty() throws ValuePlusException {
        when(transactionRepository.findByUser_IdAndReference(anyLong(), anyString()))
                .thenReturn(empty());
        var result = transferService.getTransactionByReference(agentUser, "reference");

        assertTrue(result.isEmpty());

        verify(transactionRepository).findByUser_IdAndReference(anyLong(), anyString());
    }

    @Test
    void getTransactionByReference_returnsEmpty_Admin() throws ValuePlusException {
        when(transactionRepository.findByReference(anyString()))
                .thenReturn(empty());
        var result = transferService.getTransactionByReference(adminUser, "reference");

        assertTrue(result.isEmpty());

        verify(transactionRepository).findByReference(anyString());
    }

    @Test
    void getTransactionBetween() throws ValuePlusException {
        when(transactionRepository.findByUser_IdAndCreatedAtIsBetweenOrderByIdDesc(
                anyLong(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(pageable)))
                .thenReturn(pagedTransaction);

        var result = transferService.getTransactionBetween(agentUser, LocalDate.MIN, LocalDate.MAX, pageable);

        var transaction = result.getContent().get(0);

        assertPagedResult(result);
        assertTransaction(transaction);

        verify(transactionRepository).findByUser_IdAndCreatedAtIsBetweenOrderByIdDesc(
                anyLong(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(pageable));
    }

    @Test
    void getTransactionBetween_Admin() throws ValuePlusException {
        when(transactionRepository.findByCreatedAtIsBetweenOrderByIdDesc(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(pageable)))
                .thenReturn(pagedTransaction);

        var result = transferService.getTransactionBetween(adminUser, LocalDate.MIN, LocalDate.MAX, pageable);

        var transaction = result.getContent().get(0);

        assertPagedResult(result);
        assertTransaction(transaction);

        verify(transactionRepository).findByCreatedAtIsBetweenOrderByIdDesc(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(pageable));
    }

    private void assertPagedResult(Page<TransactionModel> result) {
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    private void assertTransaction(TransactionModel transaction) {
        assertEquals("0011313333", transaction.getAccountNumber());
        assertEquals(BigDecimal.ONE, transaction.getAmount());
        assertEquals("044", transaction.getBankCode());
        assertEquals(1L, transaction.getUserId());
    }
}