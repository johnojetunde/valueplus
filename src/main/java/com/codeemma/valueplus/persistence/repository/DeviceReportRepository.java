package com.codeemma.valueplus.persistence.repository;

import com.codeemma.valueplus.persistence.entity.DeviceReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceReportRepository extends JpaRepository<DeviceReport, Long> {

    List<DeviceReport> findByAgentCodeAndYear(String agentCode, String year);

    Long countAllByAgentCode(String agentCode);
}
