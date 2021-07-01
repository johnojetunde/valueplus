package com.valueplus.persistence.entity;

import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.domain.products.ProductProviderUserModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Data
@Entity
@Table(name = "product_provider")
public class ProductProviderUser extends BasePersistentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Enumerated(EnumType.STRING)
    private ProductProvider provider;
    private String agentCode;
    private String agentUrl;

    public static ProductProviderUser toNewEntity(ProductProviderUserModel providerModel) {
        return ProductProviderUser.builder()
                .provider(providerModel.getProvider())
                .id(providerModel.getId())
                .agentUrl(providerModel.getReferralUrl())
                .agentCode(providerModel.getAgentCode())
                .build();
    }
}
