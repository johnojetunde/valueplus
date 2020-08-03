package com.codeemma.valueplus.app.controller;

import com.codeemma.valueplus.app.security.TokenAuthenticationService;
import com.codeemma.valueplus.domain.dto.LoginToken;
import com.codeemma.valueplus.domain.dto.RoleType;
import com.codeemma.valueplus.domain.dto.UserCreate;
import com.codeemma.valueplus.domain.dto.UserDto;
import com.codeemma.valueplus.domain.service.concretes.RegistrationService;
import com.codeemma.valueplus.persistence.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "v1/register", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class RegistrationController {

    private final RegistrationService registrationService;
    private final TokenAuthenticationService tokenAuthenticationService;

    public RegistrationController(RegistrationService registrationService, TokenAuthenticationService tokenAuthenticationService) {
        this.registrationService = registrationService;
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@Valid @RequestBody UserCreate userCreate) throws Exception {
        User registered = registrationService.saveUser(userCreate, RoleType.AGENT);
        return UserDto.valueOf(registered);
    }

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginToken registerAdmin(@Valid @RequestBody UserCreate userCreate) throws Exception {
        User registered = registrationService.saveUser(userCreate, RoleType.ADMIN);
        String token = tokenAuthenticationService.createUserToken(registered);
        return new LoginToken(token);
    }
}
