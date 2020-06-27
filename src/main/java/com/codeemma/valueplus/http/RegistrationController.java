package com.codeemma.valueplus.http;

import com.codeemma.valueplus.dto.RoleType;
import com.codeemma.valueplus.dto.UserCreate;
import com.codeemma.valueplus.dto.UserDto;
import com.codeemma.valueplus.model.User;
import com.codeemma.valueplus.security.TokenAuthenticationService;
import com.codeemma.valueplus.service.RegistrationService;
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
//    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@Valid @RequestBody UserCreate userCreate) {
        User registered = registrationService.saveUser(userCreate, RoleType.AGENT);
        String token = tokenAuthenticationService.createUserToken(registered);
//        httpHeaders.set(TokenAuthenticationService.AUTH_HEADER_NAME, token);
        return UserDto.valueOf(registered);
    }
}
