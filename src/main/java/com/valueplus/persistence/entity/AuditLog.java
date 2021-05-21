package com.valueplus.persistence.entity;

import com.valueplus.domain.enums.ActionType;
import com.valueplus.domain.enums.EntityType;
import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "audit_log")
public class AuditLog extends BasePersistentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String newData;
    private String prevData;
    @Enumerated(EnumType.STRING)
    private ActionType actionType;
    @Enumerated(EnumType.STRING)
    private EntityType entityType;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User actor;

    @Override
    public String toString() {
        return "AuditLog{" +
                "entityType=" + entityType +
                ", previousData='" + prevData + '\'' +
                ", newData='" + newData + '\'' +
                '}';
    }
}
