package com.valueplus.app.controller;

import com.valueplus.domain.model.SettingModel;
import com.valueplus.domain.service.abstracts.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "v1/settings", produces = APPLICATION_JSON_VALUE)
public class SettingsController {
    private final SettingsService settingsService;

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    public SettingModel update(@Valid @RequestBody SettingModel settings) {
        return settingsService.update(settings);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public Optional<SettingModel> getCurrentSetting() {
        return settingsService.getCurrentSetting();
    }
}
