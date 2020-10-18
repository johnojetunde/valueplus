package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.model.AccountSummary;
import com.codeemma.valueplus.fixtures.TestFixtures;
import com.codeemma.valueplus.persistence.entity.*;
import com.codeemma.valueplus.persistence.repository.DeviceReportRepository;
import com.codeemma.valueplus.persistence.repository.ProductOrderRepository;
import com.codeemma.valueplus.persistence.repository.TransactionRepository;
import com.codeemma.valueplus.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;

import static com.codeemma.valueplus.domain.enums.OrderStatus.COMPLETED;
import static com.codeemma.valueplus.domain.model.RoleType.ADMIN;
import static com.codeemma.valueplus.domain.util.FunctionUtil.setScale;
import static com.codeemma.valueplus.fixtures.TestFixtures.*;
import static java.math.BigDecimal.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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
    @InjectMocks
    private DefaultSummaryService summaryService;

    private User agentUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        initMocks(this);

        agentUser = mockUser();
        agentUser.setAgentCode("agentCode");

        adminUser = getUser(ADMIN);
    }

    @Test
    void getSummary() throws ValuePlusException {
        String accountNumber = "accountNumber";
        Wallet wallet = TestFixtures.getWallet(agentUser);
        wallet.setAmount(setScale(valueOf(32.10)));

        Product product = product(valueOf(9));
        List<Transaction> transactions = seedTransaction(accountNumber);
        List<ProductOrder> productOrders = seedProduct(product);

        when(deviceReportRepository.countAllByAgentCode(eq("agentCode")))
                .thenReturn(30L);
        when(productOrderRepository.findByUser_idAndStatus(eq(1L), eq(COMPLETED)))
                .thenReturn(productOrders);
        when(transactionRepository.findAllByUser_Id(eq(1L)))
                .thenReturn(transactions);

        AccountSummary summary = summaryService.getSummary(agentUser);

        assertThat(summary.getTotalAgents()).isEqualTo(1);
        assertThat(summary.getPendingWithdrawalCount()).isEqualTo(2);
        assertThat(summary.getTotalApprovedWithdrawals()).isEqualTo(setScale(BigDecimal.valueOf(25.10)));
        assertThat(summary.getTotalPendingWithdrawal()).isEqualTo(setScale(BigDecimal.valueOf(11.20)));
        assertThat(summary.getTotalProductAgentProfits()).isEqualTo(setScale(BigDecimal.valueOf(7.45)));
        assertThat(summary.getTotalProductSales()).isEqualTo(setScale(BigDecimal.valueOf(43.45)));
        assertThat(summary.getTotalActiveUsers()).isEqualTo(30);
        assertThat(summary.getTotalDownloads()).isEqualTo(0);

        verify(deviceReportRepository).countAllByAgentCode(eq("agentCode"));
        verify(productOrderRepository).findByUser_idAndStatus(eq(1L), eq(COMPLETED));
        verify(transactionRepository).findAllByUser_Id(eq(1L));
    }

    @Test
    void getSummaryAllUsers() throws ValuePlusException {
        String accountNumber = "accountNumber";
        Wallet wallet = TestFixtures.getWallet(adminUser);
        wallet.setAmount(setScale(valueOf(32.10)));

        Product product = product(valueOf(9));
        List<Transaction> transactions = seedTransaction(accountNumber);
        List<ProductOrder> productOrders = seedProduct(product);

        when(deviceReportRepository.count())
                .thenReturn(200L);
        when(userRepository.countAllByAgentCodeIsNotNull())
                .thenReturn(5L);
        when(productOrderRepository.findByStatus(eq(COMPLETED)))
                .thenReturn(productOrders);
        when(transactionRepository.findAll())
                .thenReturn(transactions);

        AccountSummary summary = summaryService.getSummaryAllUsers();

        assertThat(summary.getTotalAgents()).isEqualTo(5);
        assertThat(summary.getPendingWithdrawalCount()).isEqualTo(2);
        assertThat(summary.getTotalApprovedWithdrawals()).isEqualTo(setScale(BigDecimal.valueOf(25.10)));
        assertThat(summary.getTotalPendingWithdrawal()).isEqualTo(setScale(BigDecimal.valueOf(11.20)));
        assertThat(summary.getTotalProductAgentProfits()).isEqualTo(setScale(BigDecimal.valueOf(7.45)));
        assertThat(summary.getTotalProductSales()).isEqualTo(setScale(BigDecimal.valueOf(43.45)));
        assertThat(summary.getTotalActiveUsers()).isEqualTo(200);
        assertThat(summary.getTotalDownloads()).isEqualTo(0);


        verify(deviceReportRepository).count();
        verify(productOrderRepository).findByStatus(eq(COMPLETED));
        verify(transactionRepository).findAll();
    }

    private List<ProductOrder> seedProduct(Product product) {
        return asList(
                productOrder(agentUser, TEN, product).setQuantity(3L),
                productOrder(agentUser, valueOf(13.45), product)
        );
    }

    private List<Transaction> seedTransaction(String accountNumber) {
        return asList(
                mockTransaction(agentUser, accountNumber, valueOf(10.20), "pending"),
                mockTransaction(agentUser, accountNumber, valueOf(20.10), "success"),
                mockTransaction(agentUser, accountNumber, TEN, "error"),
                mockTransaction(agentUser, accountNumber, valueOf(5), "success"),
                mockTransaction(agentUser, accountNumber, ONE, "otp"),
                mockTransaction(agentUser, accountNumber, ONE, "failed")
        );
    }
}