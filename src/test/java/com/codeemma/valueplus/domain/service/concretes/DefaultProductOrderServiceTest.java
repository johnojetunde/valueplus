package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.enums.OrderStatus;
import com.codeemma.valueplus.domain.model.ProductOrderModel;
import com.codeemma.valueplus.persistence.entity.Product;
import com.codeemma.valueplus.persistence.entity.ProductOrder;
import com.codeemma.valueplus.persistence.repository.ProductOrderRepository;
import com.codeemma.valueplus.persistence.repository.ProductRepository;
import com.codeemma.valueplus.persistence.specs.ProductOrderSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class DefaultProductOrderServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductOrderRepository repository;
    @Mock
    private Pageable pageable;
    @InjectMocks
    private DefaultProductOrderService orderService;
    private ProductOrderModel productOrderModel;
    private ProductOrder entity;
    private Product product;

    @BeforeEach
    void setUp() {
        initMocks(this);
        productOrderModel = orderModelFixture();
        product = Product.builder()
                .id(1L)
                .price(BigDecimal.ONE)
                .build();

        entity = ProductOrder.fromModel(productOrderModel, product);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void create_WithBadData() {
        List<ProductOrderModel> orders = singletonList(productOrderModel);

        assertThatThrownBy(() -> orderService.create(orders))
                .isInstanceOf(ValuePlusException.class)
                .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                .hasMessage("Order items contains an order with id");
    }

    @Test
    void create_WithNonExistingProduct() {
        when(productRepository.findByIdIn(anyList()))
                .thenReturn(new HashSet<>());

        productOrderModel.setId(null);
        List<ProductOrderModel> orders = singletonList(productOrderModel);

        assertThatThrownBy(() -> orderService.create(orders))
                .isInstanceOf(ValuePlusException.class)
                .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                .hasMessage("Product 1 does not exist");

        verify(productRepository).findByIdIn(anyList());
    }

    @Test
    void create_WithInvalidPrice() {
        when(productRepository.findByIdIn(anyList()))
                .thenReturn(Set.of(product.setPrice(new BigDecimal(2))));
        when(repository.saveAll(anyList()))
                .thenReturn(List.of(entity));

        productOrderModel.setId(null);
        List<ProductOrderModel> orders = singletonList(productOrderModel);

        assertThatThrownBy(() -> orderService.create(orders))
                .isInstanceOf(ValuePlusException.class)
                .hasFieldOrPropertyWithValue("httpStatus", BAD_REQUEST)
                .hasMessage("Selling price must not be less than product price");

        verify(productRepository).findByIdIn(anyList());
    }

    @Test
    void create_successful() throws ValuePlusException {
        when(productRepository.findByIdIn(anyList()))
                .thenReturn(Set.of(product));
        when(repository.saveAll(anyList()))
                .thenReturn(List.of(entity));

        productOrderModel.setId(null);
        List<ProductOrderModel> orders = singletonList(productOrderModel);

        List<ProductOrderModel> result = orderService.create(orders);

        assertThat(result).isNotNull();
        assertThat(result.get(0).getCreatedAt()).isNotNull();
        assertThat(result.get(0).getCustomerName()).isEqualTo("customerName");
        assertThat(result.get(0).getProductId()).isEqualTo(1L);

        verify(productRepository).findByIdIn(anyList());
        verify(repository).saveAll(anyList());
    }

    @Test
    void getById_successful() throws ValuePlusException {
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(entity));

        ProductOrderModel result = orderService.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getCustomerName()).isEqualTo("customerName");

        verify(repository).findById(eq(1L));
    }

    @Test
    void getById_failed() {
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.get(1L))
                .isInstanceOf(ValuePlusException.class)
                .hasFieldOrPropertyWithValue("httpStatus", NOT_FOUND)
                .hasMessage("Order does not exist");

        verify(repository).findById(eq(1L));
    }

    @Test
    void getAll_successful() throws ValuePlusException {
        when(repository.findAll(eq(pageable)))
                .thenReturn(new PageImpl<>(singletonList(entity)));

        Page<ProductOrderModel> result = orderService.get(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).getCreatedAt()).isNotNull();
        assertThat(result.getContent().get(0).getCustomerName()).isEqualTo("customerName");

        verify(repository).findAll(eq(pageable));
    }

    @Test
    void getByProductId() throws ValuePlusException {
        when(repository.findByProduct_id(eq(1L), eq(pageable)))
                .thenReturn(new PageImpl<>(singletonList(entity)));

        Page<ProductOrderModel> result = orderService.getByProductId(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).getCreatedAt()).isNotNull();
        assertThat(result.getContent().get(0).getCustomerName()).isEqualTo("customerName");

        verify(repository).findByProduct_id(eq(1L), eq(pageable));
    }

    @Test
    void filterOrders_successful() throws ValuePlusException {
        when(productRepository.findById(eq(1L)))
                .thenReturn(Optional.of(product));

        when(repository.findAll(any(ProductOrderSpecification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(singletonList(entity)));

        Page<ProductOrderModel> result = orderService.filterProduct(1L,
                "customerName",
                null,
                LocalDate.now(),
                null,
                pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).getCreatedAt()).isNotNull();
        assertThat(result.getContent().get(0).getCustomerName()).isEqualTo("customerName");

        verify(productRepository).findById(eq(1L));
        verify(repository).findAll(any(ProductOrderSpecification.class), eq(pageable));
    }

    @Test
    void updateStatus_failed() {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.COMPLETED))
                .isInstanceOf(ValuePlusException.class)
                .hasMessage("Order does not exist")
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.NOT_FOUND);

        verify(repository).findById(eq(1L));
    }

    @Test
    void updateStatus_successful() throws ValuePlusException {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(entity));
        when(repository.save(any(ProductOrder.class)))
                .then(i -> i.getArgument(0, ProductOrder.class));

        ProductOrderModel result = orderService.updateStatus(1L, OrderStatus.COMPLETED);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);

        verify(repository).findById(eq(1L));
        verify(repository).save(any(ProductOrder.class));
    }

    private ProductOrderModel orderModelFixture() {
        return ProductOrderModel.builder()
                .id(1L)
                .customerName("customerName")
                .address("address")
                .quantity(10L)
                .sellingPrice(BigDecimal.ONE)
                .phoneNumber("09000000000")
                .status(OrderStatus.IN_PROGRESS)
                .productId(1L)
                .build();
    }
}