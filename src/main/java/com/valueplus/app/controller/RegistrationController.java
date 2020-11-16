package com.valueplus.app.controller;

import com.valueplus.domain.model.AgentCreate;
import com.valueplus.domain.model.AgentDto;
import com.valueplus.domain.model.UserCreate;
import com.valueplus.domain.model.UserDto;
import com.valueplus.domain.service.concretes.RegistrationService;
import com.valueplus.persistence.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.valueplus.domain.model.RoleType.ADMIN;

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
        User registered = registrationService.createAdmin(userCreate, ADMIN);
        return UserDto.valueOf(registered);
    }
}
