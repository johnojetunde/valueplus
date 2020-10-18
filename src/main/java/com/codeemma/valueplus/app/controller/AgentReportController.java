package com.codeemma.valueplus.app.controller;

import com.codeemma.valueplus.domain.service.concretes.Data4meMonthlyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "v1/reports", produces = APPLICATION_JSON_VALUE)
public class AgentReportController {

    private final Data4meMonthlyReportService data4meMonthlyReportService;

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void getAll() throws Exception {
        data4meMonthlyReportService.loadMonthlyReport();
    }
}
