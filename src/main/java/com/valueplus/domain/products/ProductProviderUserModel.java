package com.valueplus.domain.products;

import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.domain.model.AgentCreate;
import com.valueplus.persistence.entity.User;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@Builder
public class ProductProviderUserModel {
    Long id;
    String firstname;
    String lastname;
    String email;
    String password;
    String referralUrl;
    String agentCode;
    String address;
    String phone;
    ProductProvider provider;


    public static ProductProviderUserModel from(AgentCreate agent) {
        return ProductProviderUserModel.builder()
                .email(agent.getEmail())
                .firstname(agent.getFirstname())
                .lastname(agent.getLastname())
                .phone(agent.getPhone())
                .password(agent.getPassword())
                .address(agent.getAddress())
                .build();
    }

    public static ProductProviderUserModel from(User agent) {
        return ProductProviderUserModel.builder()
                .email(agent.getEmail())
                .firstname(agent.getFirstname())
                .lastname(agent.getLastname())
                .phone(agent.getPhone())
                .password(agent.getPassword())
                .address(agent.getAddress())
                .build();
    }

    public ProductProviderModel toModel() {
        return new ProductProviderModel(getAgentCode(), getReferralUrl());
    }
}
