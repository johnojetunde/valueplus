package com.codeemma.valueplus.app.controller;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.dto.AccountModel;
import com.codeemma.valueplus.domain.dto.AccountRequest;
import com.codeemma.valueplus.domain.service.abstracts.AccountService;
import com.codeemma.valueplus.domain.util.UserUtils;
import com.codeemma.valueplus.persistence.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "v1/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public AccountModel create(@Valid @RequestBody AccountRequest accountRequest) throws ValuePlusException {
        User loggedInUser = UserUtils.getLoggedInUser();
        return accountService.create(loggedInUser, accountRequest);
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AccountModel update(
            @PathVariable("id") Long id,
            @Valid @RequestBody AccountRequest accountRequest) throws ValuePlusException {
        User loggedInUser = UserUtils.getLoggedInUser();
        return accountService.update(id, loggedInUser, accountRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public AccountModel getAccount() throws ValuePlusException {
        User loggedInUser = UserUtils.getLoggedInUser();
        return accountService.getAccount(loggedInUser);
    }
}
