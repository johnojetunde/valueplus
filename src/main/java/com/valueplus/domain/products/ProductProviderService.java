package com.valueplus.domain.products;

import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.domain.model.AgentReport;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

public interface ProductProviderService {
    ProductProvider provider();

    ProductProviderUserModel create(Object authDetails, ProductProviderUserModel user);

    default ProductProviderUserModel register(ProductProviderUserModel user) {
        var authDetails = authenticate();
        var existingUser = get(authDetails, user.getEmail());
        return existingUser
                .map(s -> user.setAgentCode(s.getAgentCode())
                        .setReferralUrl(s.getReferralUrl())
                        .setProvider(provider())
                )
                .orElseGet(() -> create(authDetails, user)
                        .setProvider(provider()));
    }

    Optional<ProductProviderUserModel> get(Object authDetails, String email);

    Object authenticate();

    Set<AgentReport> downloadAgentReport(final LocalDate reportDate) throws Exception;
}
