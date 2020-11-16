package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.ProductModel;
import com.valueplus.domain.model.RoleType;
import com.valueplus.persistence.entity.Product;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.ProductRepository;
import com.valueplus.fixtures.TestFixtures;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class DefaultProductServiceTest {
    @Mock
    private ProductRepository repository;
    @Mock
    private Pageable pageable;
    @InjectMocks
    private DefaultProductService productService;
    private Product entity;

    @BeforeEach
    void setUp() {
        initMocks(this);
        ProductModel product = productFixture();
        entity = Product.fromModel(product);
        entity.setId(1L).setDeleted(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(Product.class)))
                .thenReturn(entity);
    }

    @Test
    void create_productSuccessfully() throws ValuePlusException {
        ProductModel product = productFixture();
        var result = productService.create(product);

        Assertions.assertEquals("product", result.getName());
        Assertions.assertEquals(1L, result.getId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void create_productFails() {
        when(repository.findByName(anyString()))
                .thenReturn(Optional.of(entity));
        ProductModel product = productFixture();

        assertThatThrownBy(() -> productService.create(product))
                .isInstanceOf(ValuePlusException.class)
                .hasMessage("Product with name exists");
    }

    @Test
    void update_IdBadData() {
        ProductModel product = productFixture()
                .setId(5L);

        assertThatThrownBy(() -> productService.update(1L, product))
                .isInstanceOf(ValuePlusException.class)
                .hasMessage("Id not matching with Product id")
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    void update_nonExistingProduct() {
        ProductModel product = productFixture()
                .setId(1L);

        assertThatThrownBy(() -> productService.update(1L, product))
                .isInstanceOf(ValuePlusException.class)
                .hasMessage("Product does not exist")
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.NOT_FOUND);
    }

    @Test
    void update_nameNotUnique() {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(entity));

        when(repository.findByNameAndIdIsNot(anyString(), anyLong()))
                .thenReturn(Optional.of(entity));

        ProductModel product = productFixture()
                .setId(1L);

        assertThatThrownBy(() -> productService.update(1L, product))
                .isInstanceOf(ValuePlusException.class)
                .hasMessage("Product name exists")
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    void update_successful() throws ValuePlusException {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(entity));

        when(repository.findByNameAndIdIsNot(anyString(), anyLong()))
                .thenReturn(Optional.empty());

        ProductModel product = productFixture()
                .setId(1L);

        ProductModel result = productService.update(1L, product);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void delete_fails() {
        assertThatThrownBy(() -> productService.delete(1L))
                .isInstanceOf(ValuePlusException.class)
                .hasMessage("Product does not exist")
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.NOT_FOUND);
    }

    @Test
    void delete_successful() throws ValuePlusException {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(entity));

        assertThat(productService.delete(1L)).isTrue();
    }

    @Test
    void getById_fails() {
        assertThatThrownBy(() -> productService.get(1L))
                .isInstanceOf(ValuePlusException.class)
                .hasMessage("Product does not exist")
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.NOT_FOUND);
    }

    @Test
    void getById_successful() throws ValuePlusException {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(entity));

        ProductModel result = productService.get(1L);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getAll_successfulAgent() throws ValuePlusException {
        when(repository.findProductsByDisabledFalse(any(Pageable.class)))
                .thenReturn(new PageImpl<>(singletonList(entity)));

        User agent = TestFixtures.mockUser();

        Page<ProductModel> result = productService.get(pageable, agent);

        assertThat(result.hasContent()).isTrue();
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void disableProduct_successful() throws ValuePlusException {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(entity));

        ProductModel result = productService.disable(1L);
        assertThat(result).isNotNull();
        assertThat(result.isDisabled()).isTrue();
    }


    @Test
    void enableProduct_successful() throws ValuePlusException {
        entity.setDisabled(true);

        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(entity));

        ProductModel result = productService.enable(1L);
        assertThat(result).isNotNull();
        assertThat(result.isDisabled()).isFalse();
    }

    @Test
    void getAll_successfulAdmin() throws ValuePlusException {
        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(singletonList(entity)));

        User agent = TestFixtures.getUser(RoleType.ADMIN);

        Page<ProductModel> result = productService.get(pageable, agent);

        assertThat(result.hasContent()).isTrue();
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }


    private ProductModel productFixture() {
        return ProductModel.builder()
                .name("product")
                .image("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/4QAqRXhpZgAASUkqAAgAAAABADEBAgAHAAAAGgAAAAAAAABHb29nbGUAAP")
                .price(BigDecimal.ONE)
                .description("description")
                .build();
    }
}