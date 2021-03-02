package com.valueplus.domain.service.abstracts;

import com.valueplus.domain.model.SettingModel;

import java.util.Optional;

public interface SettingsService {
    SettingModel update(SettingModel setting);

    Optional<SettingModel> getCurrentSetting();
}
