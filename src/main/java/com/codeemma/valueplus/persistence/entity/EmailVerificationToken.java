package com.codeemma.valueplus.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "email_verification_token")
public class EmailVerificationToken extends BasePersistentEntity {
    @Id
    private Long userId;
    private String verificationToken;
}
