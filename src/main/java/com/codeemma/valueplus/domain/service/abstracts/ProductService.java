package com.codeemma.valueplus.domain.service.abstracts;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.model.ProductModel;
import com.codeemma.valueplus.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductModel create(ProductModel product) throws ValuePlusException;

    ProductModel update(Long id, ProductModel product) throws ValuePlusException;

    ProductModel disable(Long id) throws ValuePlusException;

    ProductModel enable(Long id) throws ValuePlusException;


    boolean delete(Long id) throws ValuePlusException;

    ProductModel get(Long id) throws ValuePlusException;

    Page<ProductModel> get(Pageable pageable, User user) throws ValuePlusException;
}
