package com.example.reporting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body to generate an order report")
public class OrderReportRequest {

    @NotNull(message = "orderId is required")
    @Schema(description = "ID of the order from Order Service", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;
}

