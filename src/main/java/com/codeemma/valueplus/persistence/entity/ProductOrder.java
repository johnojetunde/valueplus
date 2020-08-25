package com.codeemma.valueplus.persistence.entity;

import com.codeemma.valueplus.domain.enums.OrderStatus;
import com.codeemma.valueplus.domain.model.ProductOrderModel;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

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

    public ProductOrderModel toModel() {
        return ProductOrderModel.builder()
                .id(this.id)
                .customerName(this.customerName)
                .address(this.address)
                .quantity(this.quantity)
                .sellingPrice(this.sellingPrice)
                .phoneNumber(this.phoneNumber)
                .status(this.status)
                .productId(this.product.getId())
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public static ProductOrder fromModel(ProductOrderModel model, Product product) {
        return ProductOrder.builder()
                .id(model.getId())
                .customerName(model.getCustomerName())
                .address(model.getAddress())
                .quantity(model.getQuantity())
                .sellingPrice(model.getSellingPrice())
                .phoneNumber(model.getPhoneNumber())
                .status(model.getStatus())
                .product(product)
                .build();
    }
}