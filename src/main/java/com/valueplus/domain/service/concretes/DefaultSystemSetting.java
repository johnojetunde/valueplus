package com.valueplus.domain.service.concretes;

import com.valueplus.app.config.audit.AuditEventPublisher;
import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.enums.Status;
import com.valueplus.domain.model.SettingCreateRequest;
import com.valueplus.domain.model.SettingLogModel;
import com.valueplus.domain.model.SettingModel;
import com.valueplus.domain.model.SettingScheduleModel;
import com.valueplus.domain.service.abstracts.SettingsService;
import com.valueplus.persistence.entity.Setting;
import com.valueplus.persistence.entity.SettingLog;
import com.valueplus.persistence.entity.SettingsSchedule;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.SettingRepository;
import com.valueplus.persistence.repository.SettingScheduleRepository;
import com.valueplus.persistence.repository.SettingsLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.valueplus.domain.enums.ActionType.SETTING_CHANGE;
import static com.valueplus.domain.enums.EntityType.SETTING;
import static com.valueplus.domain.enums.Status.SCHEDULED;
import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;
import static com.valueplus.domain.util.MapperUtil.copy;

@Slf4j
@RequiredArgsConstructor
@Service
public class DefaultSystemSetting implements SettingsService {
    private final SettingRepository settingRepository;
    private final SettingsLogRepository settingsLogRepository;
    private final SettingScheduleRepository settingScheduleRepository;
    private final Clock clock;
    private final AuditEventPublisher auditEvent;

    public Optional<SettingModel> getCurrentSetting() {
        return getCurrentSystemSetting().map(Setting::toModel);
    }

    @Override
    public Page<SettingLogModel> getSettingLogs(Pageable pageable) {
        return settingsLogRepository.findAll(pageable)
                .map(SettingLog::toModel);
    }

    @Override
    public Page<SettingScheduleModel> getScheduledCommission(Pageable pageable) {
        return settingScheduleRepository.findAll(pageable)
                .map(SettingsSchedule::toModel);
    }

    @Override
    public String update(SettingCreateRequest model, User loggedInUser) throws ValuePlusException {
        var date = LocalDate.now(clock);
        var settings = getCurrentSystemSetting();

        validateCommissionEffectiveDate(model, date, settings);

        var scheduled = SettingsSchedule.builder()
                .effectiveDate(model.getCommissionEffectiveDate())
                .commissionPercentage(model.getCommissionPercentage())
                .initiator(loggedInUser.getUsername())
                .status(SCHEDULED)
                .build();

        if (settings.isPresent()) {
            updateExistingSchedules();
            settingScheduleRepository.save(scheduled);
            return "Settings has been scheduled";
        }

        changeSettings(settings, scheduled);
        return "Settings successfully updated";
    }

    @Transactional
    public SettingModel changeSettings(Optional<Setting> settings, SettingsSchedule schedule) {
        updateSchedules(schedule);

        var oldObject = settings.map(s -> copy(s, Setting.class)).orElse(new Setting());
        var entity = settings.orElseGet(() -> Setting.builder()
                .commissionPercentage(schedule.getCommissionPercentage())
                .build());

        createAndSaveLog(settings, schedule);
        entity.setCommissionPercentage(schedule.getCommissionPercentage());
        entity = settingRepository.save(entity);

        auditEvent.publish(oldObject, entity, SETTING_CHANGE, SETTING);
        return entity.toModel();
    }

    public void effectCommission() {
        log.info("checking scheduled commission and effecting it");
        var date = LocalDate.now(clock);
        var schedules = settingScheduleRepository.findSettingsScheduleByStatusAndEffectiveDateIsLessThanEqual(SCHEDULED, date)
                .stream().findFirst();
        var settings = getCurrentSystemSetting();

        schedules.ifPresent(schedule -> changeSettings(settings, schedule));
    }

    private void updateSchedules(SettingsSchedule newSchedule) {
        updateExistingSchedules();

        newSchedule.setStatus(Status.EFFECTED);
        settingScheduleRepository.save(newSchedule);
    }

    private List<SettingsSchedule> updateExistingSchedules() {
        var schedules = settingScheduleRepository.findSettingsScheduleByStatus(SCHEDULED);
        schedules.forEach(s -> s.setStatus(Status.CANCELLED));
        return settingScheduleRepository.saveAll(schedules);
    }

    private SettingLog createAndSaveLog(Optional<Setting> settings, SettingsSchedule schedule) {
        var settingLog = SettingLog.builder()
                .prevCommissionPercentage(settings.map(Setting::getCommissionPercentage).orElse(BigDecimal.ZERO))
                .initiator(schedule.getInitiator())
                .commissionPercentage(schedule.getCommissionPercentage())
                .build();
        return settingsLogRepository.save(settingLog);
    }

    private void validateCommissionEffectiveDate(SettingCreateRequest model, LocalDate date, Optional<Setting> settings) throws ValuePlusException {
        if (settings.isPresent() && (date.isEqual(model.getCommissionEffectiveDate()) || date.isAfter(model.getCommissionEffectiveDate()))) {
            throw new ValuePlusException("The Effective date of the commission has to be in the future", HttpStatus.BAD_REQUEST);
        }
    }

    private Optional<Setting> getCurrentSystemSetting() {
        return emptyIfNullStream(settingRepository.findAll())
                .findFirst();
    }
}
