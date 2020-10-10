package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.app.security.UserAuthentication;
import com.codeemma.valueplus.domain.enums.OrderStatus;
import com.codeemma.valueplus.domain.model.ProductOrderModel;
import com.codeemma.valueplus.domain.service.abstracts.ProductOrderService;
import com.codeemma.valueplus.domain.service.abstracts.WalletService;
import com.codeemma.valueplus.persistence.entity.Product;
import com.codeemma.valueplus.persistence.entity.ProductOrder;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.ProductOrderRepository;
import com.codeemma.valueplus.persistence.repository.ProductRepository;
import com.codeemma.valueplus.persistence.specs.ProductOrderSpecification;
import com.codeemma.valueplus.persistence.specs.SearchCriteria;
import com.codeemma.valueplus.persistence.specs.SearchOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.codeemma.valueplus.domain.enums.OrderStatus.COMPLETED;
import static com.codeemma.valueplus.domain.model.RoleType.AGENT;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@Service
public class DefaultProductOrderService implements ProductOrderService {
    private final ProductOrderRepository repository;
    private final ProductRepository productRepository;
    private final WalletService walletService;

    @Override
    public List<ProductOrderModel> create(List<ProductOrderModel> orders, UserAuthentication authentication) throws ValuePlusException {
        boolean anyRecordWithId = orders.stream()
                .anyMatch(order -> order.getId() != null);

        if (anyRecordWithId) {
            throw new ValuePlusException("Order items contains an order with id", BAD_REQUEST);
        }

        List<Long> products = extractProductIds(orders);
        Map<Long, Product> productMap = convertToMap(productRepository.findByIdIn(products));

        ensureAllProductExists(products, productMap);

        List<ProductOrder> productOrderList = convertToEntities(orders, productMap, authentication.getDetails());

        return repository.saveAll(productOrderList).stream()
                .map(ProductOrder::toModel)
                .collect(toList());
    }

    @Override
    public ProductOrderModel get(Long id, UserAuthentication authentication) throws ValuePlusException {
        User user = authentication.getDetails();
        ProductOrder productOder;
        if (AGENT.name().equals(user.getRole().getName())) {
            productOder = getOrder(id, authentication.getDetails());
        } else {
            productOder = getOrder(id);
        }
        return productOder.toModel();
    }

    @Override
    public ProductOrderModel updateStatus(Long id, OrderStatus status, UserAuthentication authentication) throws ValuePlusException {
        User user = authentication.getDetails();
        ProductOrder productOder;
        if (AGENT.name().equals(user.getRole().getName())) {
            productOder = getOrder(id, authentication.getDetails());
        } else {
            productOder = getOrder(id);
        }

        productOder.setStatus(status);

        ProductOrder savedOrder = repository.save(productOder);

        BigDecimal userProfit = productOder.getSellingPrice().subtract(productOder.getProduct().getPrice());
        BigDecimal totalProfit = userProfit.multiply(BigDecimal.valueOf(productOder.getQuantity()));

        if (COMPLETED.equals(status)) {
            walletService.creditWallet(productOder.getUser(), totalProfit);
        }

        return savedOrder.toModel();
    }

    @Override
    public Page<ProductOrderModel> get(UserAuthentication authentication, Pageable pageable) throws ValuePlusException {
        try {
            User user = authentication.getDetails();
            Page<ProductOrder> productOders;
            if (AGENT.name().equals(user.getRole().getName())) {
                productOders = repository.findByUser_id(user.getId(), pageable);
            } else {
                productOders = repository.findAll(pageable);
            }
            return productOders.map(ProductOrder::toModel);
        } catch (Exception e) {
            throw new ValuePlusException("Error getting all orders", e);
        }
    }

    @Override
    public Page<ProductOrderModel> getByProductId(Long productId, UserAuthentication authentication, Pageable pageable) throws ValuePlusException {
        try {
            User user = authentication.getDetails();
            Page<ProductOrder> productOders;
            if (AGENT.name().equals(user.getRole().getName())) {
                productOders = repository.findByUser_idAndProduct_id(user.getId(), productId, pageable);
            } else {
                productOders = repository.findByProduct_id(productId, pageable);
            }
            return productOders.map(ProductOrder::toModel);
        } catch (Exception e) {
            throw new ValuePlusException(format("Error getting orders by productId %d", productId), e);
        }
    }

    @Override
    public Page<ProductOrderModel> filterProduct(Long productId,
                                                 String customerName,
                                                 OrderStatus status,
                                                 LocalDate startDate,
                                                 LocalDate endDate,
                                                 Pageable pageable,
                                                 UserAuthentication authentication) throws ValuePlusException {
        Product product = null;
        if (productId != null) {
            product = productRepository.findById(productId).orElse(null);
        }
        ProductOrderSpecification specification = buildSpecification(customerName, status, startDate, endDate, product, authentication.getDetails());
        return repository.findAll(Specification.where(specification), pageable)
                .map(ProductOrder::toModel);
    }

    private List<ProductOrder> convertToEntities(List<ProductOrderModel> orders, Map<Long, Product> productMap, User user) throws ValuePlusException {
        List<ProductOrder> productOrderList = new ArrayList<>();
        for (ProductOrderModel order : orders) {
            var product = productMap.get(order.getProductId());
            ensureSellingPriceIsValid(order, product);

            productOrderList.add(ProductOrder.fromModel(order, product, user)
                    .setStatus(OrderStatus.PENDING));
        }
        return productOrderList;
    }

    private void ensureSellingPriceIsValid(ProductOrderModel order, Product product) throws ValuePlusException {
        if (product.getPrice().compareTo(order.getSellingPrice()) > 0) {
            throw new ValuePlusException("Selling price must not be less than product price", BAD_REQUEST);
        }
    }

    private List<Long> extractProductIds(List<ProductOrderModel> orders) {
        return orders.parallelStream()
                .map(ProductOrderModel::getProductId)
                .distinct()
                .collect(toList());
    }

    private void ensureAllProductExists(List<Long> products, Map<Long, Product> productMap) throws ValuePlusException {
        for (Long pid : products) {
            if (!productMap.containsKey(pid)) {
                throw new ValuePlusException(format("Product %d does not exist", pid), BAD_REQUEST);
            }
        }
    }

    private Map<Long, Product> convertToMap(Set<Product> products) {
        return products.stream()
                .collect(toMap(Product::getId, Function.identity()));
    }

    private ProductOrder getOrder(Long id, User user) throws ValuePlusException {
        return repository.findByIdAndUser_id(id, user.getId())
                .orElseThrow(() -> new ValuePlusException("Order does not exist", NOT_FOUND));
    }

    private ProductOrder getOrder(Long id) throws ValuePlusException {
        return repository.findById(id)
                .orElseThrow(() -> new ValuePlusException("Order does not exist", NOT_FOUND));
    }

    private ProductOrderSpecification buildSpecification(String customerName,
                                                         OrderStatus status,
                                                         LocalDate startDate,
                                                         LocalDate endDate,
                                                         Product product,
                                                         User user) {
        ProductOrderSpecification specification = new ProductOrderSpecification();
        if (customerName != null) {
            specification.add(new SearchCriteria<>("customerName", customerName, SearchOperation.MATCH));
        }

        if (product != null) {
            specification.add(new SearchCriteria<>("product", product, SearchOperation.EQUAL));
        }

        if (status != null) {
            specification.add(new SearchCriteria<>("status", status, SearchOperation.EQUAL));
        }

        if (AGENT.name().equals(user.getRole().getName())) {
            specification.add(new SearchCriteria<>("user", user, SearchOperation.EQUAL));
        }

        if (startDate != null) {
            specification.add(new SearchCriteria<>(
                    "createdAt",
                    startDate.atTime(LocalTime.MIN),
                    SearchOperation.GREATER_THAN_EQUAL));
        }

        if (endDate != null) {
            specification.add(new SearchCriteria<>("createdAt",
                    endDate.atTime(LocalTime.MAX),
                    SearchOperation.LESS_THAN_EQUAL));
        }
        return specification;
    }
}
