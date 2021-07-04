package com.valueplus.app.config;

import com.valueplus.domain.service.abstracts.TransferService;
import com.valueplus.domain.service.concretes.AgentMonthlyReportService;
import com.valueplus.domain.service.concretes.DefaultSystemSetting;
import com.valueplus.domain.service.concretes.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JobScheduler {

    private final TransferService transferService;
    private final TokenService tokenService;
    private final AgentMonthlyReportService reportService;
    private final DefaultSystemSetting defaultSystemSetting;

    @Scheduled(cron = "${vp.verify-transactions.status.cron:0 0 0 * * ?}")
    public void scheduleVerifyPendingTransactions() {
        transferService.verifyPendingTransactions();
    }

    @Scheduled(cron = "${vp.token-delete.cron:0 0 0 * * ?}")
    public void deleteInvalidToken() {
        tokenService.deleteExpiredTokens();
    }

    @Scheduled(cron = "${vp.agent.report.cron:0 0 3 1 1/1 ?}")
    public void downloadAgentReport() {
        try {
            reportService.loadMonthlyReport();
        } catch (Exception e) {
            log.error("Error loading report", e);
        }
    }

    @Scheduled(cron = "${commission.effective-cron:0 0 0 * * ?}")
    public void updateScheduledCommission() {
        defaultSystemSetting.effectCommission();
    }
}
