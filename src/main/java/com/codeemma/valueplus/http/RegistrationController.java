package com.codeemma.valueplus.http;

import com.codeemma.valueplus.dto.LoginToken;
import com.codeemma.valueplus.dto.RoleType;
import com.codeemma.valueplus.dto.UserCreate;
import com.codeemma.valueplus.mail.EmailService;
import com.codeemma.valueplus.model.User;
import com.codeemma.valueplus.security.TokenAuthenticationService;
import com.codeemma.valueplus.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "v1/register", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class RegistrationController {

    private final RegistrationService registrationService;
    private final TokenAuthenticationService tokenAuthenticationService;
    private final EmailService emailService;

    public RegistrationController(RegistrationService registrationService, TokenAuthenticationService tokenAuthenticationService, EmailService emailService) {
        this.registrationService = registrationService;
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.emailService = emailService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoginToken register(@Valid @RequestBody UserCreate userCreate) {
        User registered = registrationService.saveUser(userCreate, RoleType.AGENT);
        String token = tokenAuthenticationService.createUserToken(registered);
        return new LoginToken(token);
    }

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginToken registerAdmin(@Valid @RequestBody UserCreate userCreate) {
        User registered = registrationService.saveUser(userCreate, RoleType.ADMIN);
        String token = tokenAuthenticationService.createUserToken(registered);
        return new LoginToken(token);
    }

}
