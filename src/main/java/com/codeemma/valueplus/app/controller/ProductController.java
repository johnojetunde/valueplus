package com.codeemma.valueplus.app.controller;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.model.ProductModel;
import com.codeemma.valueplus.domain.service.abstracts.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "v1/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ProductModel create(@Valid @RequestBody ProductModel productModel) throws ValuePlusException {
        return productService.create(productModel);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductModel update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductModel productModel) throws ValuePlusException {
        return productService.update(id, productModel);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductModel get(@PathVariable("id") Long id) throws ValuePlusException {
        return productService.get(id);
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProductModel> getAll(@PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {
        return productService.get(pageable);
    }
}
