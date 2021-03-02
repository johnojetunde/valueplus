package com.valueplus.domain.service.concretes;

import com.valueplus.domain.model.SettingModel;
import com.valueplus.domain.service.abstracts.SettingsService;
import com.valueplus.persistence.entity.Setting;
import com.valueplus.persistence.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;

@RequiredArgsConstructor
@Service
public class DefaultSystemSetting implements SettingsService {
    private final SettingRepository settingRepository;

    @Override
    public SettingModel update(SettingModel model) {
        var settings = getCurrentSystemSetting()
                .orElseGet(() -> Setting.builder()
                        .commissionPercentage(model.getCommissionPercentage())
                        .build());

        return settingRepository.save(settings).toModel();
    }

    private Optional<Setting> getCurrentSystemSetting() {
        return emptyIfNullStream(settingRepository.findAll())
                .findFirst();
    }

    public Optional<SettingModel> getCurrentSetting() {
        return getCurrentSystemSetting().map(Setting::toModel);
    }
}
