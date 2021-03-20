package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.enums.OrderStatus;
import com.valueplus.domain.mail.EmailService;
import com.valueplus.domain.model.ProductOrderModel;
import com.valueplus.domain.service.abstracts.ProductOrderService;
import com.valueplus.domain.service.abstracts.WalletService;
import com.valueplus.domain.util.FunctionUtil;
import com.valueplus.persistence.entity.Product;
import com.valueplus.persistence.entity.ProductOrder;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.ProductOrderRepository;
import com.valueplus.persistence.repository.ProductRepository;
import com.valueplus.persistence.specs.ProductOrderSpecification;
import com.valueplus.persistence.specs.SearchCriteria;
import com.valueplus.persistence.specs.SearchOperation;
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

import static com.valueplus.domain.util.UserUtils.isAgent;
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
    private final EmailService emailService;

    @Override
    public List<ProductOrderModel> create(List<ProductOrderModel> orders, User user) throws ValuePlusException {
        boolean anyRecordWithId = orders.stream()
                .anyMatch(order -> order.getId() != null);

        if (anyRecordWithId) {
            throw new ValuePlusException("Order items contains an order with id", BAD_REQUEST);
        }

        List<Long> products = extractProductIds(orders);
        Map<Long, Product> productMap = convertToMap(productRepository.findByIdIn(products));

        ensureAllProductExists(products, productMap);

        List<ProductOrder> productOrderList = convertToEntities(orders, productMap, user);

        return repository.saveAll(productOrderList).stream()
                .map(ProductOrder::toModel)
                .collect(toList());
    }

    @Override
    public ProductOrderModel get(Long id, User user) throws ValuePlusException {
        ProductOrder productOder;
        if (isAgent(user)) {
            productOder = getOrder(id, user);
        } else {
            productOder = getOrder(id);
        }
        return productOder.toModel();
    }

    @Override
    public ProductOrderModel updateStatus(Long id, OrderStatus status, User user) throws ValuePlusException {
        try {
            ProductOrder productOder;

            if (isAgent(user)) {
                productOder = getOrder(id, user);
            } else {
                productOder = getOrder(id);
            }

            validateStatusUpdateRequest(status, productOder);

            productOder.setStatus(status);

            ProductOrder savedOrder = repository.save(productOder);

            BigDecimal qty = FunctionUtil.setScale(BigDecimal.valueOf(productOder.getQuantity()));
            BigDecimal productPrice = FunctionUtil.setScale(productOder.getProduct().getPrice());
            BigDecimal sellingPrice = FunctionUtil.setScale(productOder.getSellingPrice());

            BigDecimal userProfit = sellingPrice.subtract(productPrice);
            BigDecimal totalProfit = FunctionUtil.setScale(userProfit.multiply(qty));

            emailService.sendProductOrderStatusUpdate(savedOrder.getUser(), savedOrder);

            if (OrderStatus.COMPLETED.equals(status)) {
                walletService.creditWallet(productOder.getUser(), totalProfit, format("Credit from ProductOrder completion (id: %d)", productOder.getId()));
            }

            return savedOrder.toModel();
        } catch (Exception e) {
            if (e instanceof ValuePlusException) {
                throw (ValuePlusException) e;
            }
            throw new ValuePlusException(e.getMessage());
        }
    }

    @Override
    public Page<ProductOrderModel> get(User user, Pageable pageable) throws ValuePlusException {
        try {
            Page<ProductOrder> productOders;
            if (isAgent(user)) {
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
    public Page<ProductOrderModel> getByProductId(Long productId, User user, Pageable pageable) throws ValuePlusException {
        try {
            Page<ProductOrder> productOrders;
            if (isAgent(user)) {
                productOrders = repository.findByUser_idAndProduct_id(user.getId(), productId, pageable);
            } else {
                productOrders = repository.findByProduct_id(productId, pageable);
            }
            return productOrders.map(ProductOrder::toModel);
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
                                                 User user) {
        Product product = null;
        if (productId != null) {
            product = productRepository.findById(productId).orElse(null);
        }
        ProductOrderSpecification specification = buildSpecification(customerName, status, startDate, endDate, product, user);
        return repository.findAll(Specification.where(specification), pageable)
                .map(ProductOrder::toModel);
    }

    @Override
    public List<ProductOrderModel> getAllProducts(Long productId,
                                                  String customerName,
                                                  OrderStatus status,
                                                  LocalDate startDate,
                                                  LocalDate endDate,
                                                  User user) {
        Product product = null;
        if (productId != null) {
            product = productRepository.findById(productId).orElse(null);
        }
        ProductOrderSpecification specification = buildSpecification(customerName, status, startDate, endDate, product, user);
        return repository.findAll(Specification.where(specification)).stream()
                .map(ProductOrder::toModel)
                .collect(toList());
    }

    private void validateStatusUpdateRequest(OrderStatus status, ProductOrder productOder) throws ValuePlusException {
        if (productOder.getStatus().equals(status)) {
            throw new ValuePlusException(format("ProductOrder status is presently %s", status), BAD_REQUEST);
        }
        if (OrderStatus.CANCELLED.equals(status) && !OrderStatus.PENDING.equals(productOder.getStatus())) {
            throw new ValuePlusException("Only Pending ProductOrder can be cancelled", BAD_REQUEST);
        }
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

        if (isAgent(user)) {
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
