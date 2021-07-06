package com.valueplus.persistence.repository;

import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.persistence.entity.DeviceReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceReportRepository extends JpaRepository<DeviceReport, Long> {

    List<DeviceReport> findByAgentCodeAndYear(String agentCode, String year);

    List<DeviceReport> findByAgentCodeAndYearAndProvider(String agentCode, String year, ProductProvider provider);

    List<DeviceReport> findAllByProviderIsNotNull();

    Long countAllByAgentCode(String agentCode);

    List<DeviceReport> findAllByProviderIsNull();
}
