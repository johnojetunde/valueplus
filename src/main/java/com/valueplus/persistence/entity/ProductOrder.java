package com.valueplus.persistence.entity;

import com.valueplus.domain.enums.OrderStatus;
import com.valueplus.domain.model.AgentDto;
import com.valueplus.domain.model.ProductOrderModel;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Builder
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@Table(name = "product_order")
@Accessors(chain = true)
public class ProductOrder extends BasePersistentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerName;
    private String address;
    private Long quantity;
    private BigDecimal sellingPrice;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public ProductOrderModel toModel() {
        BigDecimal totalProfit = sellingPrice.subtract(product.getPrice()).multiply(BigDecimal.valueOf(quantity));
        return ProductOrderModel.builder()
                .id(this.id)
                .customerName(this.customerName)
                .address(this.address)
                .quantity(this.quantity)
                .sellingPrice(this.sellingPrice)
                .phoneNumber(this.phoneNumber)
                .status(this.status)
                .productPrice(this.product.getPrice())
                .productId(this.product.getId())
                .productName(this.product.getName())
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .agentId(this.user.getId())
                .totalProfit(totalProfit)
                .agent(AgentDto.valueOf(this.user))
                .build();
    }

    public static ProductOrder fromModel(ProductOrderModel model, Product product, User user) {
        return ProductOrder.builder()
                .id(model.getId())
                .customerName(model.getCustomerName())
                .address(model.getAddress())
                .quantity(model.getQuantity())
                .sellingPrice(model.getSellingPrice())
                .phoneNumber(model.getPhoneNumber())
                .status(model.getStatus())
                .product(product)
                .user(user)
                .build();
    }
}
