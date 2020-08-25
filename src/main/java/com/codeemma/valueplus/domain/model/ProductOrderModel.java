package com.codeemma.valueplus.domain.model;

import com.codeemma.valueplus.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductOrderModel {

    private Long id;
    @NotNull
    private Long productId;
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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
