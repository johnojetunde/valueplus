package com.codeemma.valueplus.app.controller;

import com.codeemma.valueplus.domain.model.AccountSummary;
import com.codeemma.valueplus.domain.service.abstracts.SummaryService;
import com.codeemma.valueplus.domain.util.UserUtils;
import com.codeemma.valueplus.persistence.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.codeemma.valueplus.domain.util.UserUtils.isAgent;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "v1/summary", produces = MediaType.APPLICATION_JSON_VALUE)
public class SummaryController {

    private final SummaryService summaryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public AccountSummary getWallet() throws Exception {
        User user = UserUtils.getLoggedInUser();

        return (isAgent(user))
                ? summaryService.getSummary(user)
                : summaryService.getSummaryAllUsers();
    }
}
