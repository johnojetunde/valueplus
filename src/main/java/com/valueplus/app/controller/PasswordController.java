package com.valueplus.app.controller;

import com.valueplus.domain.model.AgentDto;
import com.valueplus.domain.model.NewPassword;
import com.valueplus.domain.model.PasswordChange;
import com.valueplus.domain.model.PasswordReset;
import com.valueplus.domain.service.concretes.PasswordService;
import com.valueplus.domain.util.UserUtils;
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
    public AgentDto change(@RequestBody PasswordChange passwordChange) {
        long userId = UserUtils.getLoggedInUser().getId();
        return AgentDto.valueOf(
                passwordService.changePassword(userId, passwordChange),
                passwordService.productUrlProvider()
        );
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/reset-password")
    public void reset(@Valid @RequestBody PasswordReset passwordReset) throws Exception {
        log.info("reset() received passwordReset = {}", passwordReset);
        passwordService.sendResetPassword(passwordReset);
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/new-password")
    public void setNewPassword(@Valid @RequestBody NewPassword newPassword) throws Exception {
        log.info("setNewPassword() received newPassword = {}", newPassword);
        passwordService.resetPassword(newPassword);
    }
}
