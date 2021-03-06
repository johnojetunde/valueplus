package com.valueplus.app.controller;


import com.valueplus.domain.model.VerifyEmail;
import com.valueplus.domain.service.concretes.EmailVerificationService;
import com.valueplus.domain.util.UserUtils;
import com.valueplus.persistence.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
public class EmailVerificationController {
    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/current/send-verify-mail")
    public void sendVerifyMail() throws Exception {
        User user = UserUtils.getLoggedInUser();
        log.info("sendVerifyMail() received userId = {}", user.getId());
        emailVerificationService.sendVerifyEmail(user);
    }

    @PostMapping("/verify-mail")
    public void verifyMail(@Valid @RequestBody VerifyEmail verifyEmail) throws Exception {
        log.info("verifyMail() received verifyMail = {}", verifyEmail);
        emailVerificationService.confirmEmail(verifyEmail.getVerificationToken());
    }
}
