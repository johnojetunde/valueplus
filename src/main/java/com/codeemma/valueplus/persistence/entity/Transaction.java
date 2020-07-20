package com.codeemma.valueplus.persistence.entity;

import com.codeemma.valueplus.domain.dto.TransactionModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
public class Transaction extends BasePersistentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountNumber;
    private String bankCode;
    private BigDecimal amount;
    private String currency;
    private String reference;
    private String status;
    private Long transferId;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public TransactionModel toModel(){
        return TransactionModel.builder()
                .id(this.id)
                .accountNumber(this.accountNumber)
                .amount(this.amount)
                .bankCode(this.bankCode)
                .reference(this.reference)
                .status(this.status)
                .currency(this.currency)
                .build();
    }
}
