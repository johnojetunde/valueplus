package com.valueplus.domain.service.concretes;

import com.valueplus.domain.model.AgentReport;
import com.valueplus.domain.model.WalletModel;
import com.valueplus.domain.service.abstracts.WalletService;
import com.valueplus.persistence.entity.DeviceReport;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.DeviceReportRepository;
import com.valueplus.persistence.repository.UserRepository;
import com.valueplus.domain.util.FunctionUtil;
import com.valueplus.fixtures.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestPropertySources({
        @TestPropertySource("classpath:application.properties"),
        @TestPropertySource("classpath:test.properties")
})
@SpringBootTest
class Data4MeMonthlyReportServiceTest {

    @Autowired
    private Data4meMonthlyReportService reportService;

    @MockBean
    private DeviceReportRepository deviceReportRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private WalletService walletService;

    private User user;

    @BeforeEach
    void setUp() {
        user = TestFixtures.mockUser();
    }

    @Test
    void getReport() throws IOException {
        reportService.loadMonthlyReport();

    }

    @Test
    void processReport() {
        LocalDate now = LocalDate.now();
        String agentCode = "agentCode";
        AgentReport report = new AgentReport(agentCode, Set.of(123, 143, 154));

        when(deviceReportRepository.findByAgentCodeAndYear(eq(agentCode), eq("2020")))
                .thenReturn(singletonList(deviceReport(agentCode)));
        when(userRepository.findByAgentCodeAndDeletedFalse(eq(agentCode)))
                .thenReturn(Optional.of(user));
        when(walletService.creditWallet(
                eq(user),
                ArgumentMatchers.eq(FunctionUtil.setScale(BigDecimal.valueOf(600.00))),
                eq("Credit via agent report")))
                .thenReturn(WalletModel.builder().build());
        when(deviceReportRepository.saveAll(anyList()))
                .thenReturn(emptyList());

        reportService.processReport(report, now);

        verify(deviceReportRepository).findByAgentCodeAndYear(eq(agentCode), eq("2020"));
        verify(userRepository).findByAgentCodeAndDeletedFalse(eq(agentCode));
        verify(walletService).creditWallet(
                eq(user),
                ArgumentMatchers.eq(FunctionUtil.setScale(BigDecimal.valueOf(600.00))),
                eq("Credit via agent report"));
        verify(deviceReportRepository).saveAll(anyList());
    }

    private DeviceReport deviceReport(String agentCode) {
        return DeviceReport.builder()
                .agentCode(agentCode)
                .year("2020")
                .deviceId(143)
                .build();
    }
}