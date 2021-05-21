package com.valueplus.persistence.entity;

import com.valueplus.domain.model.SettingLogModel;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "setting_log")
public class SettingLog extends BasePersistentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal commissionPercentage;
    private String initiator;
    private BigDecimal prevCommissionPercentage;

    public SettingLogModel toModel() {
        return SettingLogModel.builder()
                .id(id)
                .commissionPercentage(commissionPercentage)
                .initiator(initiator)
                .prevCommissionPercentage(prevCommissionPercentage)
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .build();
    }
}
