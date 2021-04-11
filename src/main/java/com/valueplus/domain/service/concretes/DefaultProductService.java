package com.valueplus.domain.service.concretes;

import com.valueplus.app.config.audit.AuditEventPublisher;
import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.ProductModel;
import com.valueplus.domain.service.abstracts.ProductService;
import com.valueplus.domain.util.UserUtils;
import com.valueplus.persistence.entity.Product;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.valueplus.domain.enums.ActionType.*;
import static com.valueplus.domain.enums.EntityType.PRODUCT;
import static com.valueplus.domain.util.MapperUtil.copy;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DefaultProductService implements ProductService {

    private final ProductRepository repository;
    private final AuditEventPublisher auditEvent;

    @Override
    public ProductModel create(ProductModel product) throws ValuePlusException {
        if (repository.findByName(product.getName()).isPresent()) {
            throw new ValuePlusException("Product with name exists");
        }
        var entity = Product.fromModel(product);
        var savedEntity = repository.save(entity);

        auditEvent.publish(new Object(), savedEntity, PRODUCT_CREATE, PRODUCT);
        return savedEntity.toModel();
    }

    @Override
    public ProductModel update(Long id, ProductModel product) throws ValuePlusException {
        if (!id.equals(product.getId())) {
            throw new ValuePlusException("Id not matching with Product id", HttpStatus.BAD_REQUEST);
        }

        Product entity = getProduct(product.getId());
        var oldObject = copy(entity, Product.class);

        Optional<Product> productWithSameName = repository.findByNameAndIdIsNot(
                product.getName(),
                product.getId());

        if (productWithSameName.isPresent()) {
            throw new ValuePlusException("Product name exists", HttpStatus.BAD_REQUEST);
        }

        entity.setDescription(product.getDescription());
        entity.setName(product.getName());
        entity.setPrice(product.getPrice());
        entity.setImage(product.getImage());

        var savedEntity = repository.save(entity);
        auditEvent.publish(oldObject, savedEntity, PRODUCT_UPDATE, PRODUCT);

        return savedEntity.toModel();
    }

    @Override
    public ProductModel disable(Long id) throws ValuePlusException {
        Product product = getProduct(id);
        var oldObject = copy(product, Product.class);

        product.setDisabled(true);

        var savedEntity = repository.save(product);
        auditEvent.publish(oldObject, savedEntity, PRODUCT_STATUS_UPDATE, PRODUCT);
        return savedEntity.toModel();

    }

    @Override
    public ProductModel enable(Long id) throws ValuePlusException {
        Product product = getProduct(id);
        var oldObject = copy(product, Product.class);

        product.setDisabled(false);

        var savedEntity = repository.save(product);
        auditEvent.publish(oldObject, savedEntity, PRODUCT_STATUS_UPDATE, PRODUCT);
        return savedEntity.toModel();
    }

    @Override
    public boolean delete(Long id) throws ValuePlusException {
        Product entity = getProduct(id);
        var oldObject = copy(entity, Product.class);

        entity.setDeleted(true);

        var savedEntity = repository.save(entity);
        auditEvent.publish(oldObject, savedEntity, PRODUCT_DELETE, PRODUCT);
        return true;
    }

    @Override
    public ProductModel get(Long id) throws ValuePlusException {
        return getProduct(id).toModel();
    }

    @Override
    public Page<ProductModel> get(Pageable pageable, User user) throws ValuePlusException {
        try {
            Page<Product> products = UserUtils.isAgent(user)
                    ? repository.findProductsByDisabledFalse(pageable)
                    : repository.findAll(pageable);

            return products.map(Product::toModel);
        } catch (Exception e) {
            throw new ValuePlusException("Error getting all products", e);
        }
    }

    private Product getProduct(Long id) throws ValuePlusException {
        return repository.findById(id)
                .orElseThrow(() -> new ValuePlusException("Product does not exist", NOT_FOUND));
    }
}
