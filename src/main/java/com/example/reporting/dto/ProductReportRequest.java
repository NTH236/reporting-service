package com.example.reporting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body to generate or manually record a product report")
public class ProductReportRequest {

    @NotNull(message = "productId is required")
    @Schema(description = "ID of the product from Product Service", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @Schema(description = "Optional Order Report ID to associate with", example = "1")
    private Long orderReportId;

    @Schema(description = "Total quantity sold (if omitted, calculated from orders or defaults to 1)", example = "2")
    private Integer totalSold;

    @Schema(description = "Selling unit price (if omitted, fetched from Product Service)", example = "1500.00")
    private BigDecimal unitPrice;

    @Schema(description = "Cost / import price (if omitted, fetched from Product Service)", example = "1100.00")
    private BigDecimal costPrice;
}

