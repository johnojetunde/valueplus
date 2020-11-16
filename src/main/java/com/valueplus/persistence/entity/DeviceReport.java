package com.valueplus.persistence.entity;

import lombok.*;

import javax.persistence.*;

@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "device_report")
public class DeviceReport extends BasePersistentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @EqualsAndHashCode.Include
    private String agentCode;
    @EqualsAndHashCode.Include
    private Integer deviceId;
    @EqualsAndHashCode.Include
    private String year;
}
