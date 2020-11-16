package com.valueplus.persistence.entity;

import com.valueplus.domain.model.AccountModel;
import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "account")
public class Account extends BasePersistentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountNumber;
    private String accountName;
    private String bankCode;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public AccountModel toModel() {
        return AccountModel.builder()
                .id(this.id)
                .accountName(this.accountName)
                .accountNumber(this.accountNumber)
                .bankCode(this.bankCode)
                .userId(this.user.getId())
                .build();
    }
}
