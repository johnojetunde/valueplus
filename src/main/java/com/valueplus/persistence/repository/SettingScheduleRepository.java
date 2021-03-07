package com.valueplus.persistence.repository;

import com.valueplus.domain.enums.Status;
import com.valueplus.persistence.entity.SettingsSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SettingScheduleRepository extends JpaRepository<SettingsSchedule, Long> {
    List<SettingsSchedule> findSettingsScheduleByStatus(Status status);

    List<SettingsSchedule> findSettingsScheduleByStatusAndEffectiveDateIsLessThanEqual(Status status, LocalDate date);
}
