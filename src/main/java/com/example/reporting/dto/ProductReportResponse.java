package com.example.reporting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product report details")
public class ProductReportResponse {

    @Schema(description = "Report item ID", example = "1")
    private Long id;

    @Schema(description = "Associated Order Report ID", example = "1")
    private Long orderReportId;

    @Schema(description = "Product ID from Product Service", example = "101")
    private Long productId;

    @Schema(description = "Product Name from Product Service (if enriched)", example = "Dell XPS 15")
    private String productName;

    @Schema(description = "Total quantity sold", example = "2")
    private Integer totalSold;

    @Schema(description = "Revenue generated (quantity * unit_price)", example = "3000.00")
    private BigDecimal revenue;

    @Schema(description = "Cost of goods (quantity * cost_price)", example = "2200.00")
    private BigDecimal cost;

    @Schema(description = "Profit achieved (revenue - cost)", example = "800.00")
    private BigDecimal profit;

    @Schema(description = "Report creation timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}

