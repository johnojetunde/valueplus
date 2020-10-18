package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.domain.model.AgentReport;
import com.codeemma.valueplus.domain.service.abstracts.WalletService;
import com.codeemma.valueplus.persistence.entity.DeviceReport;
import com.codeemma.valueplus.persistence.repository.DeviceReportRepository;
import com.codeemma.valueplus.persistence.repository.UserRepository;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.codeemma.valueplus.domain.util.FunctionUtil.setScale;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
public class Data4meMonthlyReportService {

    private final Data4meService data4MeService;
    private final DeviceReportRepository deviceReportRepository;
    private final WalletService walletService;
    private final UserRepository userRepository;
    private final Integer deviceCreditAmount;

    public Data4meMonthlyReportService(Data4meService data4MeService,
                                       DeviceReportRepository deviceReportRepository,
                                       WalletService walletService,
                                       UserRepository userRepository,
                                       @Value("${device-credit-amount:300}") Integer deviceCreditAmount) {
        this.data4MeService = data4MeService;
        this.deviceReportRepository = deviceReportRepository;
        this.walletService = walletService;
        this.userRepository = userRepository;
        this.deviceCreditAmount = deviceCreditAmount;
    }

    public void loadMonthlyReport() throws IOException {
        LocalDate reportDate = LocalDate.now().minusMonths(1);
        var result = data4MeService.downloadAgentReport(reportDate);

        if (result.isEmpty()) return;


        Set<AgentReport> reportContent = new HashSet<>();
        FileUtils.readLines(new File(result.get()))
                .forEach(line -> reportContent.add(toAgentReport(line.toString())));

        reportContent.forEach(report -> processReport(report, reportDate));
    }

    private AgentReport toAgentReport(String file) {
        List<String> content = asList(file.split(","));
        String agentCode = content.get(0);
        Set<Integer> deviceIds = content.subList(1, content.size())
                .stream()
                .map(Integer::parseInt)
                .collect(toSet());

        return new AgentReport(agentCode, deviceIds);
    }

    void processReport(AgentReport report, LocalDate date) {
        if (report.getDeviceIds().isEmpty()) return;

        String year = String.valueOf(date.getYear());

        List<DeviceReport> deviceReports = deviceReportRepository.findByAgentCodeAndYear(report.getAgentCode(), year);
        List<DeviceReport> reports = mapToDeviceReport(report, year);

        reports.removeAll(deviceReports);

        BigDecimal creditAmount = setScale(BigDecimal.valueOf(reports.size() * deviceCreditAmount));

        userRepository.findByAgentCodeAndDeletedFalse(report.getAgentCode())
                .map(user -> walletService.creditWallet(user, creditAmount, "Credit via agent report"));

        deviceReportRepository.saveAll(reports);
    }

    private List<DeviceReport> mapToDeviceReport(AgentReport report, String year) {
        return report.getDeviceIds().stream()
                .map(id -> toDeviceReport(report.getAgentCode(), id, year))
                .collect(toList());
    }

    private DeviceReport toDeviceReport(String agentCode, Integer deviceId, String year) {
        return DeviceReport.builder()
                .agentCode(agentCode)
                .deviceId(deviceId)
                .year(year)
                .build();
    }
}
