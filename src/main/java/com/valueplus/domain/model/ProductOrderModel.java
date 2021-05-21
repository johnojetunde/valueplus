package com.valueplus.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.valueplus.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductOrderModel {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @NotNull
    private Long productId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String productName;
    @NotBlank
    private String customerName;
    @NotBlank
    private String address;
    @NotNull
    private Long quantity;
    @NotNull
    private BigDecimal sellingPrice;
    @NotBlank
    private String phoneNumber;
    private OrderStatus status;
    private Long agentId;
    private AgentDto agent;

    private BigDecimal productPrice;
    private BigDecimal totalProfit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
