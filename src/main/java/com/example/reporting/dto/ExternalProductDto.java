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
public class ExternalProductDto {

    private Long id;

    private String name;

    @JsonAlias({"unit_price", "unitPrice", "price"})
    private BigDecimal unitPrice;

    @JsonAlias({"cost_price", "costPrice", "cost"})
    private BigDecimal costPrice;

    @JsonAlias({"stock_quantity", "stockQuantity", "quantity", "stock"})
    private Integer stockQuantity;

    private String description;
}

