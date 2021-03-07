package com.valueplus.persistence.entity;

import com.valueplus.domain.enums.Status;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "setting_schedule")
public class SettingsSchedule extends BasePersistentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal commissionPercentage;
    private LocalDate effectiveDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String initiator;
}
