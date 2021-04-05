package com.valueplus.persistence.entity;

import com.valueplus.domain.model.ProductModel;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Builder
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@Table(name = "product")
@Accessors(chain = true)
public class Product extends BasePersistentEntity implements ToModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private BigDecimal price;
    @Column(nullable = false)
    private String image;
    @Column(nullable = false)
    private boolean deleted;
    @Column(nullable = false)
    private boolean disabled;

    public ProductModel toModel() {
        return ProductModel.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .price(this.price)
                .image(this.image)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .disabled(this.disabled)
                .build();
    }

    public static Product fromModel(ProductModel model) {
        return Product.builder()
                .name(model.getName())
                .description(model.getDescription())
                .price(model.getPrice())
                .image(model.getImage())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Product product = (Product) o;
        return id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
