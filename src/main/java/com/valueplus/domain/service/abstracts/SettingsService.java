package com.valueplus.domain.service.abstracts;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.SettingCreateRequest;
import com.valueplus.domain.model.SettingLogModel;
import com.valueplus.domain.model.SettingModel;
import com.valueplus.domain.model.SettingScheduleModel;
import com.valueplus.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SettingsService {
    String update(SettingCreateRequest setting, User loggedInUser) throws ValuePlusException;

    Optional<SettingModel> getCurrentSetting();

    Page<SettingLogModel> getSettingLogs(Pageable pageable);

    Page<SettingScheduleModel> getScheduledCommission(Pageable pageable);
}
