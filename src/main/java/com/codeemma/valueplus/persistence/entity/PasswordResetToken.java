package com.codeemma.valueplus.persistence.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken extends BasePersistentEntity{
    @Id
    private Long userId;
    private String resetToken;
}
