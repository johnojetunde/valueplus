package com.valueplus.domain.products;

import lombok.Getter;
import lombok.Value;

@Getter
@Value
public class ProductProviderModel {
    String agentCode;
    String referralUrl;
}
