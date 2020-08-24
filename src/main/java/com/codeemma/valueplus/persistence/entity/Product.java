package com.codeemma.valueplus.persistence.entity;

import com.codeemma.valueplus.domain.model.ProductModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Builder
@NoArgsConstructor
@Setter
@AllArgsConstructor
@Table(name = "product")
@Accessors(chain = true)
public class Product extends BasePersistentEntity {

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

    public ProductModel toModel() {
        return ProductModel.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .price(this.price)
                .image(this.image)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .deleted(this.deleted)
                .build();
    }

    public static Product fromModel(ProductModel model) {
        return Product.builder()
                .name(model.getName())
                .description(model.getDescription())
                .price(model.getPrice())
                .image(model.getImage())
                .deleted(model.isDeleted())
                .build();
    }
}
