package com.valueplus.app.controller;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.AccountModel;
import com.valueplus.domain.model.AccountRequest;
import com.valueplus.domain.service.abstracts.AccountService;
import com.valueplus.domain.util.UserUtils;
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
        return accountService.create(UserUtils.getLoggedInUser(), accountRequest);
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AccountModel update(
            @PathVariable("id") Long id,
            @Valid @RequestBody AccountRequest accountRequest) throws ValuePlusException {
        return accountService.update(id, UserUtils.getLoggedInUser(), accountRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public AccountModel getAccount() throws ValuePlusException {
        return accountService.getAccount(UserUtils.getLoggedInUser());
    }
}
