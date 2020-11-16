package com.valueplus.persistence.entity;

import com.valueplus.domain.enums.TransactionType;
import com.valueplus.domain.model.WalletHistoryModel;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "wallet_history")
public class WalletHistory extends BasePersistentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private String description;
    @OneToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    public WalletHistoryModel toModel() {
        return WalletHistoryModel.builder()
                .walletHistoryId(this.id)
                .walletId(this.wallet.getId())
                .amount(amount)
                .type(this.type)
                .description(this.description)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
