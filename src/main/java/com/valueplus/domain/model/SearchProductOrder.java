package com.valueplus.domain.model;

import com.valueplus.domain.enums.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SearchProductOrder {
    private String customerName;
    private Long productId;
    private OrderStatus status;
    private String startDate;
    private String endDate;
    private Long agentId;
}
