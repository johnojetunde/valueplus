package com.valueplus.persistence.repository;

import com.valueplus.persistence.entity.SettingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsLogRepository extends JpaRepository<SettingLog, Long> {
}
