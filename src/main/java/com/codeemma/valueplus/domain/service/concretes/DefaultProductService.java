package com.codeemma.valueplus.domain.service.concretes;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.model.ProductModel;
import com.codeemma.valueplus.domain.service.abstracts.ProductService;
import com.codeemma.valueplus.persistence.entity.Product;
import com.codeemma.valueplus.persistence.entity.User;
import com.codeemma.valueplus.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.codeemma.valueplus.domain.util.UserUtils.isAgent;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DefaultProductService implements ProductService {

    private final ProductRepository repository;

    @Override
    public ProductModel create(ProductModel product) throws ValuePlusException {
        if (repository.findByName(product.getName()).isPresent()) {
            throw new ValuePlusException("Product with name exists");
        }
        var entity = Product.fromModel(product);

        return repository.save(entity).toModel();
    }

    @Override
    public ProductModel update(Long id, ProductModel product) throws ValuePlusException {
        if (!id.equals(product.getId())) {
            throw new ValuePlusException("Id not matching with Product id", HttpStatus.BAD_REQUEST);
        }

        Product entity = getProduct(product.getId());
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

        return repository.save(entity).toModel();
    }

    @Override
    public ProductModel disable(Long id) throws ValuePlusException {
        Product product = getProduct(id);
        product.setDisabled(true);

        return repository.save(product).toModel();
    }

    @Override
    public ProductModel enable(Long id) throws ValuePlusException {
        Product product = getProduct(id);
        product.setDisabled(false);

        return repository.save(product).toModel();
    }

    @Override
    public boolean delete(Long id) throws ValuePlusException {
        Product entity = getProduct(id);

        entity.setDeleted(true);

        repository.save(entity);
        return true;
    }

    @Override
    public ProductModel get(Long id) throws ValuePlusException {
        return getProduct(id).toModel();
    }

    @Override
    public Page<ProductModel> get(Pageable pageable, User user) throws ValuePlusException {
        try {
            Page<Product> products = isAgent(user)
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
