package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.model.AccountModel;
import com.codeemma.valueplus.domain.model.TransactionModel;
import com.codeemma.valueplus.domain.service.abstracts.PaymentService;
import com.codeemma.valueplus.persistence.entity.Transaction;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.AccountRepository;
import com.codeemma.valueplus.persistence.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.codeemma.valueplus.fixtures.TestFixtures.*;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class DefaultTransferServiceTest {

    @Mock
    private PaymentService paymentService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private Pageable pageable;

    @InjectMocks
    private DefaultTransferService transferService;

    private User mockUser;
    private Transaction transaction;
    private Page<Transaction> pagedTransaction;

    @BeforeEach
    void setUp() throws ValuePlusException {
        String accountNumber = "0011313333";
        initMocks(this);
        mockUser = mockUser();
        transaction = mockTransaction(accountNumber);
        pagedTransaction = new PageImpl<>(singletonList(transaction));
        when(accountRepository.findByUser_Id(anyLong()))
                .thenReturn(Optional.of(mockAccount(accountNumber)));
        when(paymentService.transfer(any(AccountModel.class), any(BigDecimal.class)))
                .thenReturn(mockTransferResponse(BigDecimal.valueOf(100)));
        when(transactionRepository.save(any(Transaction.class)))
                .then(i -> i.getArgument(0, Transaction.class));

    }

    @Test
    void transfer() throws ValuePlusException {
        var requestModel = mockPaymentRequestModel(BigDecimal.TEN);
        TransactionModel model = transferService.transfer(mockUser, requestModel);

        verify(accountRepository).findByUser_Id(anyLong());
        verify(paymentService).transfer(any(AccountModel.class), any(BigDecimal.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void getAllUserTransactions() throws ValuePlusException {
        when(transactionRepository.findByUser_IdOrderByIdDesc(anyLong(), eq(pageable)))
                .thenReturn(pagedTransaction);

        var result = transferService.getAllUserTransactions(mockUser, pageable);

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

        var result = transferService.getTransactionByReference(mockUser, "reference");

        assertTrue(result.isPresent());
        assertTransaction(result.get());

        verify(transactionRepository).findByUser_IdAndReference(anyLong(), anyString());
    }

    @Test
    void getTransactionByReference_returnsEmpty() throws ValuePlusException {
        when(transactionRepository.findByUser_IdAndReference(anyLong(), anyString()))
                .thenReturn(empty());
        var result = transferService.getTransactionByReference(mockUser, "reference");

        assertTrue(result.isEmpty());

        verify(transactionRepository).findByUser_IdAndReference(anyLong(), anyString());
    }

    @Test
    void getTransactionBetween() throws ValuePlusException {
        when(transactionRepository.findByUser_IdAndCreatedAtIsBetweenOrderByIdDesc(
                anyLong(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(pageable)))
                .thenReturn(pagedTransaction);

        var result = transferService.getTransactionBetween(mockUser, LocalDate.MIN, LocalDate.MAX, pageable);

        var transaction = result.getContent().get(0);

        assertPagedResult(result);
        assertTransaction(transaction);

        verify(transactionRepository).findByUser_IdAndCreatedAtIsBetweenOrderByIdDesc(
                anyLong(),
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