package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.dto.AccountRequest;
import com.codeemma.valueplus.domain.service.abstracts.PaymentService;
import com.codeemma.valueplus.persistence.entity.Account;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.AccountRepository;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static com.codeemma.valueplus.fixtures.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class DefaultAccountServiceTest {
    private static final String ACCOUNT_NUMBER = "083039383933";
    @Mock
    private PaymentService paymentService;
    @Mock
    private AccountRepository repository;

    @InjectMocks
    private DefaultAccountService accountService;

    private User mockUser;

    @BeforeEach
    void setUp() throws ValuePlusException {
        initMocks(this);

        mockUser = mockUser();
        when(repository.findByUser_Id(anyLong()))
                .thenReturn(Optional.of(mockAccount(ACCOUNT_NUMBER)));
        when(paymentService.resolveAccountNumber(anyString(), anyString()))
                .then(i -> mockAccountNumberModel(i.getArgument(0, String.class)));
        when(repository.save(any(Account.class)))
                .then(i -> i.getArgument(0, Account.class));
    }

    @Test
    void createAccount_successful() throws ValuePlusException {
        AccountRequest request = new AccountRequest(ACCOUNT_NUMBER, "044");

        var result = accountService.create(mockUser, request);

        assertEquals(ACCOUNT_NUMBER, result.getAccountNumber());
        assertEquals("Value Plus", result.getAccountName());
        assertEquals("044", result.getBankCode());

        verify(paymentService).resolveAccountNumber(anyString(), anyString());
        verify(repository).save(any(Account.class));
    }

    @Test
    void createAccount_failed() throws ValuePlusException {
        when(repository.save(any(Account.class)))
                .thenThrow(new HibernateException("Unable to save"));
        AccountRequest request = new AccountRequest("000131313", "044");

        assertThrows(ValuePlusException.class,
                () -> accountService.create(mockUser, request));

        verify(paymentService).resolveAccountNumber(anyString(), anyString());
        verify(repository).save(any(Account.class));
    }

    @Test
    void update_failed() throws ValuePlusException {
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(mockAccount(ACCOUNT_NUMBER)));
        when(repository.save(any(Account.class)))
                .thenThrow(new HibernateException("Unable to save"));

        AccountRequest request = new AccountRequest("000131313", "044");

        assertThrows(ValuePlusException.class, () -> accountService.update(1L, mockUser, request));

        verify(paymentService).resolveAccountNumber(anyString(), anyString());
        verify(repository).findById(eq(1L));
        verify(repository).save(any(Account.class));
    }

    @Test
    void update_successful() throws ValuePlusException {
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(mockAccount(ACCOUNT_NUMBER)));
        AccountRequest request = new AccountRequest("000131313", "044");

        var result = accountService.update(1L, mockUser, request);

        assertEquals("000131313", result.getAccountNumber());
        assertEquals("Value Plus", result.getAccountName());
        assertEquals("044", result.getBankCode());

        verify(paymentService).resolveAccountNumber(anyString(), anyString());
        verify(repository).findById(eq(1L));
        verify(repository).save(any(Account.class));
    }

    @Test
    void retrieve_successful() throws ValuePlusException {
        when(repository.findByUser_Id(eq(1L)))
                .thenReturn(Optional.of(mockAccount(ACCOUNT_NUMBER)));

        var result = accountService.getAccount(mockUser);

        assertEquals(ACCOUNT_NUMBER, result.getAccountNumber());
        assertEquals("Value Plus", result.getAccountName());
        assertEquals("044", result.getBankCode());
        verify(repository).findByUser_Id(eq(1L));
    }

}