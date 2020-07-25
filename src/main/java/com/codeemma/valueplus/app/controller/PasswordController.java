package com.codeemma.valueplus.app.controller;

import com.codeemma.valueplus.domain.dto.PasswordReset;
import com.codeemma.valueplus.domain.dto.PasswordChange;
import com.codeemma.valueplus.domain.dto.UserDto;
import com.codeemma.valueplus.domain.service.concretes.PasswordService;
import com.codeemma.valueplus.domain.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class PasswordController {
    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/current/password-change")
    public UserDto change(@RequestBody PasswordChange passwordChange) {
        long userId = UserUtils.getLoggedInUser().getId();
        return UserDto.valueOf(
                passwordService.changePassword(userId, passwordChange)
        );
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/current/reset-password")
    public void reset(@Valid @RequestBody PasswordReset passwordReset) throws Exception {
        log.info("reset() received passwordReset = {}", passwordReset);
        passwordService.resetPassword(passwordReset);
    }
}
