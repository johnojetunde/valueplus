package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.mail.EmailService;
import com.codeemma.valueplus.domain.model.RoleType;
import com.codeemma.valueplus.domain.model.WalletHistoryModel;
import com.codeemma.valueplus.domain.model.WalletModel;
import com.codeemma.valueplus.domain.service.abstracts.WalletHistoryService;
import com.codeemma.valueplus.fixtures.TestFixtures;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.entity.Wallet;
import com.codeemma.valueplus.persistence.repository.UserRepository;
import com.codeemma.valueplus.persistence.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.codeemma.valueplus.domain.enums.TransactionType.CREDIT;
import static com.codeemma.valueplus.domain.enums.TransactionType.DEBIT;
import static com.codeemma.valueplus.domain.util.FunctionUtil.setScale;
import static com.codeemma.valueplus.fixtures.TestFixtures.getUser;
import static com.codeemma.valueplus.fixtures.TestFixtures.mockUser;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

class DefaultWalletServiceTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletHistoryService walletHistoryService;
    @Mock
    private EmailService emailService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private DefaultWalletService walletService;
    @Mock
    private Pageable pageable;

    private User agentUser;

    @BeforeEach
    void setUp() {
        initMocks(this);

        agentUser = mockUser();
    }


    @Test
    void createWallet_existingWallet() {
        Wallet wallet = TestFixtures.getWallet(agentUser);
        when(walletRepository.findWalletByUser_Id(eq(1L)))
                .thenReturn(Optional.of(wallet));

        WalletModel walletModel = walletService.createWallet(agentUser);

        assertThat(walletModel.getAmount()).isEqualTo(wallet.getAmount());
        assertThat(walletModel.getWalletId()).isEqualTo(wallet.getId());
        assertThat(walletModel.getUserId()).isEqualTo(wallet.getUser().getId());

        verify(walletRepository).findWalletByUser_Id(eq(1L));
    }

    @Test
    void createWallet_newWallet() {
        when(walletRepository.findWalletByUser_Id(eq(1L)))
                .thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class)))
                .then(i -> {
                    var wallet = i.getArgument(0, Wallet.class);
                    wallet.setId(1L);
                    return wallet;
                });

        WalletModel walletModel = walletService.createWallet(agentUser);

        assertThat(walletModel.getAmount()).isEqualTo(setScale(BigDecimal.ZERO));
        assertThat(walletModel.getWalletId()).isEqualTo(1L);
        assertThat(walletModel.getUserId()).isEqualTo(1L);

        verify(walletRepository).findWalletByUser_Id(eq(1L));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void getUserWallet() throws ValuePlusException {
        when(walletRepository.findWalletByUser_Id(eq(1L)))
                .thenReturn(Optional.of(TestFixtures.getWallet(agentUser)));

        WalletModel walletModel = walletService.getWallet(agentUser);
        assertThat(walletModel.getAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(walletModel.getWalletId()).isEqualTo(1L);
        assertThat(walletModel.getUserId()).isEqualTo(1L);

        verify(walletRepository).findWalletByUser_Id(eq(1L));
    }

    @Test
    void getWallet() throws ValuePlusException {
        User adminUser = getUser(RoleType.ADMIN);
        when(userService.getAdminUserAccount())
                .thenReturn(Optional.of(adminUser));
        when(walletRepository.findWalletByUser_Id(eq(1L)))
                .thenReturn(Optional.of(TestFixtures.getWallet(adminUser)));

        WalletModel walletModel = walletService.getWallet();
        assertThat(walletModel.getAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(walletModel.getWalletId()).isEqualTo(1L);
        assertThat(walletModel.getUserId()).isEqualTo(1L);

        verify(walletRepository).findWalletByUser_Id(eq(1L));
    }

    @Test
    void getAllWallet() throws ValuePlusException {
        var pagedEntity = new PageImpl<>(singletonList(TestFixtures.getWallet(agentUser)));
        when(walletRepository.findWalletByUser_IdNot(eq(2L), eq(pageable)))
                .thenReturn(pagedEntity);
        when(userService.getAdminUserId())
                .thenReturn(2L);

        Page<WalletModel> wallets = walletService.getAllWallet(pageable);

        var walletModel = wallets.getContent().get(0);
        assertThat(wallets).hasSize(1);
        assertThat(walletModel.getAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(walletModel.getWalletId()).isEqualTo(1L);
        assertThat(walletModel.getUserId()).isEqualTo(1L);

        verify(walletRepository).findWalletByUser_IdNot(eq(2L), eq(pageable));
    }

    @Test
    void createWalletForAllUsers() {
        User user = mockUser();
        user.setId(2L);

        User user2 = mockUser();
        user2.setId(3L);

        Wallet wallet = TestFixtures.getWallet(user);

        when(walletRepository.findAll())
                .thenReturn(singletonList(wallet));

        when(userRepository.findUsersByDeletedFalse())
                .thenReturn(List.of(user, user2, agentUser));

        when(walletRepository.saveAll(anyList()))
                .then(i -> i.getArgument(0));

        List<WalletModel> wallets = walletService.createWalletForAllUsers();

        List<Long> userIds = wallets.stream()
                .map(WalletModel::getUserId)
                .collect(toList());

        assertThat(wallets).hasSize(2);
        assertThat(userIds).containsOnly(1L, 3L);

        verify(walletRepository).findAll();
        verify(walletRepository).saveAll(anyList());
        verify(userRepository).findUsersByDeletedFalse();
    }

    @Test
    void creditWallet() throws Exception {
        Wallet wallet = TestFixtures.getWallet(agentUser);
        wallet.setAmount(ONE);

        when(walletRepository.findWalletByUser_Id(eq(1L)))
                .thenReturn(Optional.of(wallet));
        when(walletHistoryService.createHistoryRecord(any(Wallet.class), eq(TEN), eq(CREDIT), eq("description")))
                .thenReturn(WalletHistoryModel.builder().build());
        when(walletRepository.save(any(Wallet.class)))
                .then(i -> i.getArgument(0, Wallet.class));
        doNothing()
                .when(emailService).sendCreditNotification(eq(agentUser), eq(TEN));

        WalletModel walletModel = walletService.creditWallet(agentUser, TEN, "description");

        assertThat(walletModel.getAmount()).isEqualTo(setScale(BigDecimal.valueOf(11)));
        verify(walletRepository).findWalletByUser_Id(eq(1L));
        verify(emailService).sendCreditNotification(eq(agentUser), eq(setScale(TEN)));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void debitWallet() throws Exception {
        Wallet wallet = TestFixtures.getWallet(agentUser);
        wallet.setAmount(TEN);

        when(walletRepository.findWalletByUser_Id(eq(1L)))
                .thenReturn(Optional.of(wallet));
        when(walletHistoryService.createHistoryRecord(any(Wallet.class), eq(TEN), eq(DEBIT), eq("description")))
                .thenReturn(WalletHistoryModel.builder().build());
        when(walletRepository.save(any(Wallet.class)))
                .then(i -> i.getArgument(0, Wallet.class));
        doNothing()
                .when(emailService).sendDebitNotification(eq(agentUser), eq(ONE));

        WalletModel walletModel = walletService.debitWallet(agentUser, ONE, "description");

        assertThat(walletModel.getAmount()).isEqualTo(setScale(BigDecimal.valueOf(9)));
        verify(walletRepository).findWalletByUser_Id(eq(1L));
        verify(emailService).sendDebitNotification(eq(agentUser), eq(setScale(ONE)));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void debitWallet_failed() throws Exception {
        Wallet wallet = TestFixtures.getWallet(agentUser);
        wallet.setAmount(TEN);

        when(walletRepository.findWalletByUser_Id(eq(1L)))
                .thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.debitWallet(agentUser, BigDecimal.valueOf(20), "Description"))
                .isInstanceOf(ValuePlusException.class)
                .hasFieldOrPropertyWithValue("message", "Amount to be debited more than the balance in user's wallet")
                .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST);

        verify(walletRepository).findWalletByUser_Id(eq(1L));
    }
}