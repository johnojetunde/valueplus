package com.codeemma.valueplus.app.controller;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.app.security.UserAuthentication;
import com.codeemma.valueplus.domain.enums.OrderStatus;
import com.codeemma.valueplus.domain.model.ProductOrderModel;
import com.codeemma.valueplus.domain.service.abstracts.ProductOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.codeemma.valueplus.domain.enums.OrderStatus.CANCELLED;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "v1/product-orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductOrderController {
    private final ProductOrderService productOrderService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductOrderModel> create(@AuthenticationPrincipal UserAuthentication user,
                                          @Valid @RequestBody List<ProductOrderModel> orders) throws ValuePlusException {
        return productOrderService.create(orders, user);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductOrderModel get(@PathVariable("id") Long id, @AuthenticationPrincipal UserAuthentication user) throws ValuePlusException {
        return productOrderService.get(id, user);
    }

    @GetMapping("/product/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProductOrderModel> getByProductId(@PathVariable("id") Long productId,
                                                  @PageableDefault(sort = "id", direction = DESC) Pageable pageable,
                                                  @AuthenticationPrincipal UserAuthentication user) throws ValuePlusException {
        return productOrderService.getByProductId(productId, user, pageable);
    }

    @GetMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProductOrderModel> searchProduct(@RequestParam(value = "customerName", required = false) String customerName,
                                                 @RequestParam(value = "productId", required = false) Long productId,
                                                 @RequestParam(value = "status", required = false) OrderStatus status,
                                                 @RequestParam(value = "startDate", required = false) String startDate,
                                                 @RequestParam(value = "endDate", required = false) String endDate,
                                                 @PageableDefault(sort = "id", direction = DESC) Pageable pageable,
                                                 @AuthenticationPrincipal UserAuthentication user) throws ValuePlusException {

        return productOrderService.filterProduct(
                productId,
                customerName,
                status,
                toDate(startDate),
                toDate(endDate),
                pageable,
                user);
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ProductOrderModel cancelOrder(@PathVariable("id") Long orderId,
                                         @AuthenticationPrincipal UserAuthentication user) throws ValuePlusException {
        return productOrderService.updateStatus(orderId, CANCELLED, user);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{id}/status/{status}/update")
    @ResponseStatus(HttpStatus.OK)
    public ProductOrderModel updateStatus(@PathVariable("id") Long orderId,
                                          @PathVariable("status") OrderStatus status,
                                          @AuthenticationPrincipal UserAuthentication user) throws ValuePlusException {
        return productOrderService.updateStatus(orderId, status, user);
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProductOrderModel> getAll(@PageableDefault(sort = "id", direction = DESC) Pageable pageable,
                                          @AuthenticationPrincipal UserAuthentication user) throws ValuePlusException {
        return productOrderService.get(user, pageable);
    }

    private LocalDate toDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return (date != null) ? LocalDate.parse(date, formatter) : null;
    }
}
