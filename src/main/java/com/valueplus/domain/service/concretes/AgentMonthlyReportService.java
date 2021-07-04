package com.valueplus.domain.service.concretes;

import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.domain.model.AgentReport;
import com.valueplus.domain.products.ProductProviderService;
import com.valueplus.domain.service.abstracts.WalletService;
import com.valueplus.persistence.entity.DeviceReport;
import com.valueplus.persistence.repository.DeviceReportRepository;
import com.valueplus.persistence.repository.ProductProviderUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;
import static com.valueplus.domain.util.FunctionUtil.setScale;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class AgentMonthlyReportService {

    private final DeviceReportRepository deviceReportRepository;
    private final WalletService walletService;
    private final Integer deviceCreditAmount;
    private final List<ProductProviderService> providerServices;
    private final ProductProviderUserRepository providerUserRepository;
    private final Clock clock;

    public AgentMonthlyReportService(DeviceReportRepository deviceReportRepository,
                                     WalletService walletService,
                                     @Value("${device-credit-amount:300}") Integer deviceCreditAmount,
                                     List<ProductProviderService> providerServices,
                                     ProductProviderUserRepository providerUserRepository,
                                     Clock clock) {
        this.deviceReportRepository = deviceReportRepository;
        this.walletService = walletService;
        this.deviceCreditAmount = deviceCreditAmount;
        this.providerServices = providerServices;
        this.providerUserRepository = providerUserRepository;
        this.clock = clock;
    }

    public void loadMonthlyReport() {
        LocalDate reportDate = LocalDate.now(clock).minusMonths(3);
        log.info("getting monthly agent report for {}", reportDate);

        var providerReportsFutures = emptyIfNullStream(providerServices)
                .map(p -> loadProviderReport(p, reportDate))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(providerReportsFutures)
                .join();
    }

    private CompletableFuture<Void> loadProviderReport(ProductProviderService productProviderService, LocalDate reportDate) {
        return runAsync(() -> {
            var provider = productProviderService.provider();
            log.info("getting {} monthly agent report for {}", provider, reportDate);

            var reportContent = productProviderService.downloadAgentReport(reportDate);
            reportContent.forEach(report -> processReport(report, reportDate, provider));
            log.info("finished {} monthly agent report for {}", provider, reportDate);
        }).handleAsync((__, e) -> {
            if (e != null) {
                log.error(String.format("error loading agent report for %s provider for %s", productProviderService.provider(), reportDate), e);
            }
            return __;
        });
    }

    private void processReport(AgentReport report, LocalDate date, ProductProvider provider) {
        if (report.getDeviceIds().isEmpty()) return;

        String year = String.valueOf(date.getYear());

        List<DeviceReport> deviceReports = deviceReportRepository.findByAgentCodeAndYearAndProvider(report.getAgentCode(), year, provider);
        List<DeviceReport> reports = mapToDeviceReport(report, year, provider);

        reports.removeAll(deviceReports);

        if (reports.isEmpty()) return;

        BigDecimal creditAmount = setScale(BigDecimal.valueOf((long) reports.size() * deviceCreditAmount));

        providerUserRepository.findByAgentCodeAndProvider(report.getAgentCode(), provider)
                .map(providerUser -> walletService.creditWallet(providerUser.getUser(), creditAmount, "Credit via agent report"));

        deviceReportRepository.saveAll(reports);
    }

    private List<DeviceReport> mapToDeviceReport(AgentReport report, String year, ProductProvider provider) {
        return report.getDeviceIds().stream()
                .map(id -> toDeviceReport(report.getAgentCode(), id, year, provider))
                .collect(toList());
    }

    private DeviceReport toDeviceReport(String agentCode, String deviceId, String year, ProductProvider provider) {
        return DeviceReport.builder()
                .agentCode(agentCode)
                .deviceId(deviceId)
                .year(year)
                .provider(provider)
                .build();
    }
}
