package com.valueplus.domain.model;

import com.valueplus.domain.enums.ActionType;
import com.valueplus.domain.enums.EntityType;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogModel {
    private ActionType action;
    private Object previousData;
    private Object newData;
    private EntityType entityType;
    private LocalDateTime createdAt;
    private ActorDetails actor;
    private String description;

    @Value
    public static class ActorDetails {
        Long userId;
        String email;
        String firstname;
        String lastname;
        String photo;
    }
}
