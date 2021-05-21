package com.valueplus.persistence.entity;

import com.valueplus.domain.enums.TransactionStatus;
import com.valueplus.domain.model.TransactionModel;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "transaction")
public class Transaction extends BasePersistentEntity implements ToModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountNumber;
    private String bankCode;
    private BigDecimal amount;
    private String currency;
    @NaturalId
    private String reference;
    private String status;
    private Long transferId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public TransactionModel toModel() {
        return TransactionModel.builder()
                .id(this.id)
                .accountNumber(this.accountNumber)
                .amount(this.amount)
                .bankCode(this.bankCode)
                .reference(this.reference)
                .status(TransactionStatus.resolve(this.status))
                .paystackStatus(this.status)
                .currency(this.currency)
                .userId(this.user.getId())
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

}
