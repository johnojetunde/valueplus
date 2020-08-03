package com.codeemma.valueplus.persistence.entity;

import com.codeemma.valueplus.domain.dto.TransactionModel;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.math.BigDecimal;

import static com.codeemma.valueplus.domain.enums.TransactionStatus.resolve;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "transaction")
public class Transaction extends BasePersistentEntity {

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
                .status(resolve(this.status))
                .currency(this.currency)
                .userId(this.user.getId())
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

}
