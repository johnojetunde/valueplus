package com.codeemma.valueplus.app.config;

import com.codeemma.valueplus.domain.service.abstracts.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class JobScheduler {

    private final TransferService transferService;

    @Scheduled(cron = "${vp.verify-transactions.status.cron:0 0 0 * * ?}")
    public void scheduleDirectDebitJob() {
        transferService.verifyPendingTransactions();
    }

}
