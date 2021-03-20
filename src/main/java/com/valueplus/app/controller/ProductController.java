package com.valueplus.app.controller;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.ProductModel;
import com.valueplus.domain.service.abstracts.ProductService;
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

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "v1/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasAuthority('CREATE_PRODUCT')")
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ProductModel create(@Valid @RequestBody ProductModel productModel) throws ValuePlusException {
        return productService.create(productModel);
    }

    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductModel update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductModel productModel) throws ValuePlusException {
        return productService.update(id, productModel);
    }

    @PreAuthorize("hasAuthority('DISABLE_PRODUCT')")
    @PostMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.OK)
    public ProductModel disable(@PathVariable("id") Long id) throws ValuePlusException {
        return productService.disable(id);
    }

    @PreAuthorize("hasAuthority('ENABLE_PRODUCT')")
    @PostMapping("/{id}/enable")
    @ResponseStatus(HttpStatus.OK)
    public ProductModel enable(@PathVariable("id") Long id) throws ValuePlusException {
        return productService.enable(id);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductModel get(@PathVariable("id") Long id) throws ValuePlusException {
        return productService.get(id);
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProductModel> getAll(@PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {
        return productService.get(pageable, UserUtils.getLoggedInUser());
    }
}
