package com.valueplus.app.controller;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.enums.OrderStatus;
import com.valueplus.domain.model.ProductOrderModel;
import com.valueplus.domain.service.abstracts.ProductOrderService;
import com.valueplus.domain.util.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "v1/product-orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductOrderController {

    private final ProductOrderService productOrderService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductOrderModel> create(@Valid @RequestBody List<ProductOrderModel> orders) throws ValuePlusException {
        return productOrderService.create(orders, UserUtils.getLoggedInUser());
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductOrderModel get(@PathVariable("id") Long id) throws ValuePlusException {
        return productOrderService.get(id, UserUtils.getLoggedInUser());
    }

    @GetMapping("/product/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProductOrderModel> getByProductId(@PathVariable("id") Long productId,
                                                  @PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {
        return productOrderService.getByProductId(productId, UserUtils.getLoggedInUser(), pageable);
    }

    @GetMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProductOrderModel> searchProduct(@RequestParam(value = "customerName", required = false) String customerName,
                                                 @RequestParam(value = "productId", required = false) Long productId,
                                                 @RequestParam(value = "status", required = false) OrderStatus status,
                                                 @RequestParam(value = "startDate", required = false) String startDate,
                                                 @RequestParam(value = "endDate", required = false) String endDate,
                                                 @PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {

        return productOrderService.filterProduct(
                productId,
                customerName,
                status,
                toDate(startDate),
                toDate(endDate),
                pageable,
                UserUtils.getLoggedInUser());
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ProductOrderModel cancelOrder(@PathVariable("id") Long orderId) throws ValuePlusException {
        return productOrderService.updateStatus(orderId, OrderStatus.CANCELLED, UserUtils.getLoggedInUser());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{id}/status/{status}/update")
    @ResponseStatus(HttpStatus.OK)
    public ProductOrderModel updateStatus(@PathVariable("id") Long orderId,
                                          @PathVariable("status") OrderStatus status) throws ValuePlusException {
        return productOrderService.updateStatus(orderId, status, UserUtils.getLoggedInUser());
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProductOrderModel> getAll(@PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {
        return productOrderService.get(UserUtils.getLoggedInUser(), pageable);
    }

    private LocalDate toDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return (date != null) ? LocalDate.parse(date, formatter) : null;
    }
}
