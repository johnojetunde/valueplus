package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.enums.OrderStatus;
import com.valueplus.domain.model.AccountSummary;
import com.valueplus.domain.util.FunctionUtil;
import com.valueplus.fixtures.TestFixtures;
import com.valueplus.persistence.entity.*;
import com.valueplus.persistence.repository.DeviceReportRepository;
import com.valueplus.persistence.repository.ProductOrderRepository;
import com.valueplus.persistence.repository.TransactionRepository;
import com.valueplus.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static com.valueplus.domain.model.RoleType.ADMIN;
import static com.valueplus.domain.model.RoleType.SUPER_AGENT;
import static com.valueplus.fixtures.TestFixtures.getUser;
import static com.valueplus.fixtures.TestFixtures.mockUser;
import static java.math.BigDecimal.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class DefaultSummaryServiceTest {

    @Mock
    private ProductOrderRepository productOrderRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DeviceReportRepository deviceReportRepository;
    private DefaultSummaryService summaryService;

    private User agentUser;
    private User adminUser;
    private User superAgentUser;

    @BeforeEach
    void setUp() {
        initMocks(this);
        Clock clock = Clock.systemDefaultZone();
        summaryService = new DefaultSummaryService(userRepository, productOrderRepository, transactionRepository, deviceReportRepository, clock);

        agentUser = mockUser();
        agentUser.setAgentCode("agentCode");

        adminUser = getUser(ADMIN);
        superAgentUser = getUser(SUPER_AGENT);
    }

    @Test
    void getSummary() throws ValuePlusException {
        String accountNumber = "accountNumber";
        Wallet wallet = TestFixtures.getWallet(agentUser);
        wallet.setAmount(FunctionUtil.setScale(valueOf(32.10)));

        Product product = TestFixtures.product(valueOf(9));
        List<Transaction> transactions = seedTransaction(accountNumber);
        List<ProductOrder> productOrders = seedProduct(product);

        when(deviceReportRepository.countAllByAgentCode(eq("agentCode")))
                .thenReturn(30L);
        when(productOrderRepository.findByUser_idAndStatus(eq(1L), eq(OrderStatus.COMPLETED)))
                .thenReturn(productOrders);
        when(transactionRepository.findAllByUser_Id(eq(1L)))
                .thenReturn(transactions);

        AccountSummary summary = summaryService.getSummary(agentUser);

        assertThat(summary.getTotalAgents()).isEqualTo(1);
        assertThat(summary.getPendingWithdrawalCount()).isEqualTo(2);
        assertThat(summary.getTotalApprovedWithdrawals()).isEqualTo(FunctionUtil.setScale(BigDecimal.valueOf(25.10)));
        assertThat(summary.getTotalPendingWithdrawal()).isEqualTo(FunctionUtil.setScale(BigDecimal.valueOf(11.20)));
        assertThat(summary.getTotalProductAgentProfits()).isEqualTo(FunctionUtil.setScale(BigDecimal.valueOf(7.45)));
        assertThat(summary.getTotalProductSales()).isEqualTo(FunctionUtil.setScale(BigDecimal.valueOf(43.45)));
        assertThat(summary.getTotalActiveUsers()).isEqualTo(30);
        assertThat(summary.getTotalDownloads()).isEqualTo(0);

        verify(deviceReportRepository).countAllByAgentCode(eq("agentCode"));
        verify(productOrderRepository).findByUser_idAndStatus(eq(1L), eq(OrderStatus.COMPLETED));
        verify(transactionRepository).findAllByUser_Id(eq(1L));
    }

    @Test
    void getSummary_SuperAgent() throws ValuePlusException {
        String accountNumber = "accountNumber";
        Wallet wallet = TestFixtures.getWallet(superAgentUser);
        wallet.setAmount(FunctionUtil.setScale(valueOf(32.10)));

        Product product = TestFixtures.product(valueOf(9));
        List<Transaction> transactions = seedTransaction(accountNumber);
        List<ProductOrder> productOrders = seedProduct(product);
        superAgentUser.setReferralCode("referralCode");

        when(userRepository.findUsersBySuperAgent_ReferralCode(eq("referralCode")))
                .thenReturn(List.of(mockUser()));
        when(userRepository.findActiveSuperAgentListUsers(
                isA(LocalDateTime.class), isA(LocalDateTime.class), eq("referralCode")))
                .thenReturn(List.of(mockUser()));

        when(productOrderRepository.findByUser_idAndStatus(eq(1L), eq(OrderStatus.COMPLETED)))
                .thenReturn(productOrders);
        when(transactionRepository.findAllByUser_Id(eq(1L)))
                .thenReturn(transactions);

        AccountSummary summary = summaryService.getSummary(superAgentUser);

        assertThat(summary.getTotalAgents()).isEqualTo(1);
        assertThat(summary.getPendingWithdrawalCount()).isEqualTo(2);
        assertThat(summary.getTotalApprovedWithdrawals()).isEqualTo(FunctionUtil.setScale(BigDecimal.valueOf(25.10)));
        assertThat(summary.getTotalPendingWithdrawal()).isEqualTo(FunctionUtil.setScale(BigDecimal.valueOf(11.20)));
        assertThat(summary.getTotalProductAgentProfits()).isEqualTo(FunctionUtil.setScale(BigDecimal.valueOf(7.45)));
        assertThat(summary.getTotalProductSales()).isEqualTo(FunctionUtil.setScale(BigDecimal.valueOf(43.45)));
        assertThat(summary.getTotalActiveUsers()).isEqualTo(1);
        assertThat(summary.getTotalDownloads()).isEqualTo(0);

        verify(productOrderRepository).findByUser_idAndStatus(eq(1L), eq(OrderStatus.COMPLETED));
        verify(transactionRepository).findAllByUser_Id(eq(1L));
        verify(userRepository).findUsersBySuperAgent_ReferralCode(eq("referralCode"));
        verify(userRepository).findActiveSuperAgentListUsers(isA(LocalDateTime.class), isA(LocalDateTime.class), eq("referralCode"));
    }

    @Test
    void getSummaryAllUsers() throws ValuePlusException {
        String accountNumber = "accountNumber";
        Wallet wallet = TestFixtures.getWallet(adminUser);
        wallet.setAmount(FunctionUtil.setScale(valueOf(32.10)));

        Product product = TestFixtures.product(valueOf(9));
        List<Transaction> transactions = seedTransaction(accountNumber);
        List<ProductOrder> productOrders = seedProduct(product);

        when(deviceReportRepository.count())
                .thenReturn(200L);
        when(userRepository.countUserByRole_NameIn(anyList()))
                .thenReturn(5L);
        when(productOrderRepository.findByStatus(eq(OrderStatus.COMPLETED)))
                .thenReturn(productOrders);
        when(transactionRepository.findAll())
                .thenReturn(transactions);

        AccountSummary summary = summaryService.getSummaryAllUsers();

        assertThat(summary.getTotalAgents()).isEqualTo(5);
        assertThat(summary.getPendingWithdrawalCount()).isEqualTo(2);
        assertThat(summary.getTotalApprovedWithdrawals()).isEqualTo(FunctionUtil.setScale(BigDecimal.valueOf(25.10)));
        assertThat(summary.getTotalPendingWithdrawal()).isEqualTo(FunctionUtil.setScale(BigDecimal.valueOf(11.20)));
        assertThat(summary.getTotalProductAgentProfits()).isEqualTo(FunctionUtil.setScale(BigDecimal.valueOf(7.45)));
        assertThat(summary.getTotalProductSales()).isEqualTo(FunctionUtil.setScale(BigDecimal.valueOf(43.45)));
        assertThat(summary.getTotalActiveUsers()).isEqualTo(200);
        assertThat(summary.getTotalDownloads()).isEqualTo(0);


        verify(deviceReportRepository).count();
        verify(productOrderRepository).findByStatus(eq(OrderStatus.COMPLETED));
        verify(transactionRepository).findAll();
    }

    private List<ProductOrder> seedProduct(Product product) {
        return asList(
                TestFixtures.productOrder(agentUser, TEN, product).setQuantity(3L),
                TestFixtures.productOrder(agentUser, valueOf(13.45), product)
        );
    }

    private List<Transaction> seedTransaction(String accountNumber) {
        return asList(
                TestFixtures.mockTransaction(agentUser, accountNumber, valueOf(10.20), "pending"),
                TestFixtures.mockTransaction(agentUser, accountNumber, valueOf(20.10), "success"),
                TestFixtures.mockTransaction(agentUser, accountNumber, TEN, "error"),
                TestFixtures.mockTransaction(agentUser, accountNumber, valueOf(5), "success"),
                TestFixtures.mockTransaction(agentUser, accountNumber, ONE, "otp"),
                TestFixtures.mockTransaction(agentUser, accountNumber, ONE, "failed")
        );
    }
}