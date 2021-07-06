package com.valueplus.app.controller;

import com.valueplus.domain.model.AgentCreate;
import com.valueplus.domain.model.AgentDto;
import com.valueplus.domain.model.UserCreate;
import com.valueplus.domain.model.UserDto;
import com.valueplus.domain.service.concretes.RegistrationService;
import com.valueplus.persistence.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.valueplus.domain.model.RoleType.ADMIN;
import static org.springframework.http.HttpStatus.CREATED;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "v1/register", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class RegistrationController {

    private final RegistrationService registrationService;

    @PreAuthorize("permitAll()")
    @PostMapping
    @ResponseStatus(CREATED)
    public AgentDto register(@Valid @RequestBody AgentCreate agentCreate) throws Exception {
        User registered = registrationService.createAgent(agentCreate);
        return AgentDto.valueOf(registered, registrationService.productUrlProvider());
    }

    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    @PostMapping("/admin")
    @ResponseStatus(CREATED)
    public UserDto registerAdmin(@Valid @RequestBody UserCreate userCreate) throws Exception {
        User registered = registrationService.createAdmin(userCreate, ADMIN);
        return UserDto.valueOf(registered);
    }

    @PreAuthorize("hasAuthority('CREATE_SUPER_AGENT')")
    @PostMapping("/super-agent")
    @ResponseStatus(CREATED)
    public UserDto registerSuperAgent(@Valid @RequestBody UserCreate userCreate) throws Exception {
        User registered = registrationService.createSuperAgent(userCreate);
        return UserDto.valueOf(registered);
    }
}
