package com.valueplus.domain.products;

import com.valueplus.domain.enums.ProductProvider;

public interface ProductProviderUrlService {
    ProductProvider provider();

    String getReferralUrl(String agentCode, String agentUrl);
}
