package com.valueplus.persistence.entity;

import com.valueplus.domain.model.SettingModel;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "setting")
public class Setting extends BasePersistentEntity implements ToModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal commissionPercentage;

    public SettingModel toModel() {
        return SettingModel.builder()
                .commissionPercentage(this.commissionPercentage)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
