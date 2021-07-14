package com.valueplus.domain.products;

import com.valueplus.domain.enums.ProductProvider;
import com.valueplus.domain.model.AgentReport;
import org.slf4j.Logger;

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

    default Optional<ProductProviderUserModel> migrate(ProductProviderUserModel user, Logger log) {
        try {
            var authDetails = authenticate();
            var existingUser = get(authDetails, user.getEmail());

            if (existingUser.isPresent()) {
                return existingUser
                        .map(s -> user.setAgentCode(s.getAgentCode())
                                .setReferralUrl(s.getReferralUrl())
                                .setProvider(provider())
                        );
            }
            return Optional.of(create(authDetails, user)
                    .setProvider(provider()));
        } catch (Exception e) {
            log.error("Error migrating user {}", user, e);
        }
        return Optional.empty();
    }

    Optional<ProductProviderUserModel> get(Object authDetails, String email);

    Object authenticate();

    Set<AgentReport> downloadAgentReport(final LocalDate reportDate);
}
