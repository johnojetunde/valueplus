package com.valueplus.persistence.entity;

import com.valueplus.domain.enums.ProductProvider;
import lombok.*;

import javax.persistence.*;

@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "device_report")
public class DeviceReport extends BasePersistentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @EqualsAndHashCode.Include
    private String agentCode;
    @EqualsAndHashCode.Include
    private String deviceId;
    @EqualsAndHashCode.Include
    private String year;
    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    private ProductProvider provider;
}
