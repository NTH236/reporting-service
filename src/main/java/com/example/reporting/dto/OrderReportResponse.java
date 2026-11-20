package com.example.reporting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Order financial report summary")
public class OrderReportResponse {

    @Schema(description = "Order Report ID", example = "1")
    private Long id;

    @Schema(description = "Order ID from Order Service", example = "123")
    private Long orderId;

    @Schema(description = "Total revenue from all items in this order", example = "3075.00")
    private BigDecimal totalRevenue;

    @Schema(description = "Total cost of goods sold for this order", example = "2245.00")
    private BigDecimal totalCost;

    @Schema(description = "Total profit for this order (total_revenue - total_cost)", example = "830.00")
    private BigDecimal totalProfit;

    @Schema(description = "Report creation timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "List of product breakdown reports included in this order")
    @Builder.Default
    private List<ProductReportResponse> productReports = new ArrayList<>();
}

