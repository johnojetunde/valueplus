package com.valueplus.persistence.repository;

import com.valueplus.persistence.entity.DeviceReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceReportRepository extends JpaRepository<DeviceReport, Long> {

    List<DeviceReport> findByAgentCodeAndYear(String agentCode, String year);

    Long countAllByAgentCode(String agentCode);
}
