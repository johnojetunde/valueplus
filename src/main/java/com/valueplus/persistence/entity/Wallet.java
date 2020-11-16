package com.valueplus.persistence.entity;

import com.valueplus.domain.model.WalletModel;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "wallet")
public class Wallet extends BasePersistentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public WalletModel toModel() {
        return WalletModel.builder()
                .walletId(this.id)
                .userId(this.user.getId())
                .amount(this.amount)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
