package com.example.reporting.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalOrderItemDto {

    private Long id;

    @JsonAlias({"product_id", "productId"})
    private Long productId;

    @JsonAlias({"product_name", "productName", "name"})
    private String productName;

    private Integer quantity;

    @JsonAlias({"unit_price", "unitPrice", "price"})
    private BigDecimal unitPrice;

    @JsonAlias({"total_price", "totalPrice", "amount"})
    private BigDecimal totalPrice;
}

