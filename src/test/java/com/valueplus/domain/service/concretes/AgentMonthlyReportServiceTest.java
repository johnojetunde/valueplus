package com.valueplus.domain.service.concretes;

import com.valueplus.domain.model.AgentReport;
import com.valueplus.domain.model.WalletModel;
import com.valueplus.domain.products.ProductProviderService;
import com.valueplus.domain.service.abstracts.WalletService;
import com.valueplus.domain.util.FunctionUtil;
import com.valueplus.persistence.entity.DeviceReport;
import com.valueplus.persistence.repository.DeviceReportRepository;
import com.valueplus.persistence.repository.ProductProviderUserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.valueplus.domain.enums.ProductProvider.DATA4ME;
import static com.valueplus.fixtures.TestFixtures.providerUser;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestPropertySources({
        @TestPropertySource("classpath:application.properties"),
        @TestPropertySource("classpath:test.properties")
})
@SpringBootTest
class AgentMonthlyReportServiceTest {

    @Autowired
    private AgentMonthlyReportService reportService;
    @MockBean
    private Clock clock;
    @MockBean
    private DeviceReportRepository deviceReportRepository;
    @MockBean
    private List<ProductProviderService> productProviderService;
    @MockBean
    private ProductProviderUserRepository productProviderUserRepository;
    @MockBean
    private WalletService walletService;

    @Test
    void processReport() {
        Clock fixedClock = Clock.fixed(Instant.parse("2020-04-29T10:15:30.00Z"), ZoneId.systemDefault());
        LocalDate now = LocalDate.now(fixedClock);
        when(clock.instant())
                .thenReturn(fixedClock.instant());
        when(clock.getZone())
                .thenReturn(fixedClock.getZone());
        var mockedProductProvider = Mockito.mock(ProductProviderService.class);

        when(productProviderService.stream())
                .thenReturn(Stream.of(mockedProductProvider));

        String agentCode = "agentCode";
        AgentReport report = new AgentReport(agentCode, Set.of("123", "143", "154"));
        var productProvider = providerUser(agentCode, DATA4ME);

        when(mockedProductProvider.provider())
                .thenReturn(DATA4ME);
        when(mockedProductProvider.downloadAgentReport(isA(LocalDate.class)))
                .thenReturn(Set.of(report));
        when(deviceReportRepository.findByAgentCodeAndYearAndProvider(agentCode, "2020", DATA4ME))
                .thenReturn(singletonList(deviceReport(agentCode)));
        when(productProviderUserRepository.findByAgentCodeAndProvider(agentCode, DATA4ME))
                .thenReturn(Optional.of(productProvider));
        when(walletService.creditWallet(
                productProvider.getUser(),
                FunctionUtil.setScale(BigDecimal.valueOf(600.00)),
                "Credit via agent report"))
                .thenReturn(WalletModel.builder().build());
        when(deviceReportRepository.saveAll(anyList()))
                .thenReturn(emptyList());

        reportService.loadMonthlyReport();

        verify(deviceReportRepository).findByAgentCodeAndYearAndProvider(agentCode, "2020", DATA4ME);
        verify(productProviderUserRepository).findByAgentCodeAndProvider(agentCode, DATA4ME);
        verify(walletService).creditWallet(
                productProvider.getUser(),
                FunctionUtil.setScale(BigDecimal.valueOf(600.00)),
                "Credit via agent report");
        verify(mockedProductProvider).provider();
        verify(mockedProductProvider).downloadAgentReport(isA(LocalDate.class));
        verify(deviceReportRepository).saveAll(anyList());
    }

    private DeviceReport deviceReport(String agentCode) {
        return DeviceReport.builder()
                .agentCode(agentCode)
                .year("2020")
                .deviceId("143")
                .provider(DATA4ME)
                .build();
    }
}