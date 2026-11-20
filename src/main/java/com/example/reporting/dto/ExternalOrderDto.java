package com.example.reporting.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalOrderDto {

    private Long id;

    @JsonAlias({"customer_id", "customerId", "userId"})
    private String customerId;

    @JsonAlias({"customer_name", "customerName"})
    private String customerName;

    private String status;

    @JsonAlias({"total_amount", "totalPrice", "totalAmount"})
    private BigDecimal totalAmount;

    @JsonAlias({"order_items", "items", "orderItems"})
    @Builder.Default
    private List<ExternalOrderItemDto> items = new ArrayList<>();
}

