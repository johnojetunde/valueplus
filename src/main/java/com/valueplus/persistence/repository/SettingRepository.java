package com.valueplus.persistence.repository;

import com.valueplus.persistence.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<Setting, Long> {
}
