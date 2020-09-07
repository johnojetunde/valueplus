package com.codeemma.valueplus.app.config;

import com.codeemma.valueplus.domain.service.abstracts.TransferService;
import com.codeemma.valueplus.domain.service.concretes.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class JobScheduler {

    private final TransferService transferService;
    private final TokenService tokenService;

    @Scheduled(cron = "${vp.verify-transactions.status.cron:0 0 0 * * ?}")
    public void scheduleVerifyPendingTransactions() {
        transferService.verifyPendingTransactions();
    }

    @Scheduled(cron = "${vp.token-delete.cron:0 0 0 * * ?}")
    public void deleteInvalidToken() {
        tokenService.deleteExpiredTokens();
    }
}
