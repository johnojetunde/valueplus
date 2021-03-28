package com.valueplus.domain.service.abstracts;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.enums.OrderStatus;
import com.valueplus.domain.model.ProductOrderModel;
import com.valueplus.domain.model.SearchProductOrder;
import com.valueplus.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ProductOrderService {

    List<ProductOrderModel> create(List<ProductOrderModel> order, User user) throws ValuePlusException;

    ProductOrderModel get(Long id, User user) throws ValuePlusException;

    ProductOrderModel updateStatus(Long id, OrderStatus status, User user) throws ValuePlusException;

    Page<ProductOrderModel> get(User user, Pageable pageable) throws ValuePlusException;

    Page<ProductOrderModel> getByProductId(Long productId, User vpUser, Pageable pageable) throws ValuePlusException;

    Page<ProductOrderModel> filterProduct(Long productId,
                                          String customerName,
                                          OrderStatus status,
                                          LocalDate startDate,
                                          LocalDate endDate,
                                          Pageable pageable,
                                          User user) throws ValuePlusException;

    Page<ProductOrderModel> searchProduct(SearchProductOrder searchProductOrder,
                                          Pageable pageable,
                                          User user) throws ValuePlusException;
}
