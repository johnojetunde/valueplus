package com.valueplus.app.controller;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.SettingCreateRequest;
import com.valueplus.domain.model.SettingLogModel;
import com.valueplus.domain.model.SettingModel;
import com.valueplus.domain.model.SettingScheduleModel;
import com.valueplus.domain.service.abstracts.SettingsService;
import com.valueplus.domain.util.UserUtils;
import com.valueplus.persistence.entity.User;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "v1/setting", produces = APPLICATION_JSON_VALUE)
public class SettingsController {
    private final SettingsService settingsService;

    @PreAuthorize("hasAuthority('UPDATE_SETTINGS')")
    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String update(@Valid @RequestBody SettingCreateRequest settings) throws ValuePlusException {
        User loggedInUser = UserUtils.getLoggedInUser();
        return settingsService.update(settings, loggedInUser);
    }

    @GetMapping
    @ApiResponses({@ApiResponse(code = 200, message = "success", response = SettingModel.class)})
    public SettingModel getCurrentSetting() {
        return settingsService.getCurrentSetting().orElse(null);
    }

    @PreAuthorize("hasAuthority('VIEW_AUDIT_LOG')")
    @GetMapping("/logs")
    public Page<SettingLogModel> getSettingLogs(@PageableDefault(sort = "id", direction = DESC) Pageable pageable) {
        return settingsService.getSettingLogs(pageable);
    }

    @PreAuthorize("hasAuthority('VIEW_SETTINGS_SCHEDULE')")
    @GetMapping("/schedules")
    public Page<SettingScheduleModel> getSettingScheduleLogs(@PageableDefault(sort = "id", direction = DESC) Pageable pageable) {
        return settingsService.getScheduledCommission(pageable);
    }
}
