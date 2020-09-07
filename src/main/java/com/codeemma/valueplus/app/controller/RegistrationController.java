package com.codeemma.valueplus.app.controller;

import com.codeemma.valueplus.domain.model.*;
import com.codeemma.valueplus.domain.service.concretes.RegistrationService;
import com.codeemma.valueplus.persistence.entity.User;
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

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentDto register(@Valid @RequestBody AgentCreate agentCreate) throws Exception {
        User registered = registrationService.createAgent(agentCreate);
        return AgentDto.valueOf(registered);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto registerAdmin(@Valid @RequestBody UserCreate userCreate) throws Exception {
        User registered = registrationService.createAdmin(userCreate, RoleType.ADMIN);
        return UserDto.valueOf(registered);
    }
}
