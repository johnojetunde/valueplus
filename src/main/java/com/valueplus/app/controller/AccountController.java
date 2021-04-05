package com.valueplus.app.controller;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.AccountModel;
import com.valueplus.domain.model.AccountRequest;
import com.valueplus.domain.service.abstracts.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.valueplus.domain.util.UserUtils.getLoggedInUser;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "v1/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public AccountModel create(@Valid @RequestBody AccountRequest accountRequest) throws ValuePlusException {
        return accountService.create(getLoggedInUser(), accountRequest);
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AccountModel update(
            @PathVariable("id") Long id,
            @Valid @RequestBody AccountRequest accountRequest) throws ValuePlusException {
        return accountService.update(id, getLoggedInUser(), accountRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public AccountModel getAccount() throws ValuePlusException {
        return accountService.getAccount(getLoggedInUser());
    }
}
