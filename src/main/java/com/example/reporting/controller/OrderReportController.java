package com.example.reporting.controller;

import com.example.reporting.dto.ErrorResponse;
import com.example.reporting.dto.OrderReportRequest;
import com.example.reporting.dto.OrderReportResponse;
import com.example.reporting.service.OrderReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Reports", description = "APIs for analyzing and reporting order finances (Revenue, Cost, Profit)")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderReportController {

    private final OrderReportService orderReportService;

    @PostMapping
    @Operation(summary = "Generate order report", description = "Fetches order items from Order Service and cost prices from Product Service, computes totals and persists the report.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Report generated successfully",
                    content = @Content(schema = @Schema(implementation = OrderReportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid order data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found in Order Service",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "External service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrderReportResponse> createOrderReport(
            @Valid @RequestBody OrderReportRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        log.info("REST request to generate report for order ID: {}", request.getOrderId());
        OrderReportResponse response = orderReportService.createOrderReport(request, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all order reports", description = "Retrieves all persisted order reports along with their product breakdowns.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of reports retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderReportResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<OrderReportResponse>> getAllOrderReports() {
        log.info("REST request to get all order reports");
        return ResponseEntity.ok(orderReportService.getAllOrderReports());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order report by ID", description = "Retrieves the financial breakdown for a specific order report.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report found",
                    content = @Content(schema = @Schema(implementation = OrderReportResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Report not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrderReportResponse> getOrderReportById(
            @Parameter(description = "ID of the order report", example = "1") @PathVariable("id") Long id) {
        log.info("REST request to get order report ID: {}", id);
        return ResponseEntity.ok(orderReportService.getOrderReportById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete order report", description = "Deletes an order report and cascade deletes associated product reports.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Report deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Report not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteOrderReport(
            @Parameter(description = "ID of the order report to delete", example = "1") @PathVariable("id") Long id) {
        log.info("REST request to delete order report ID: {}", id);
        orderReportService.deleteOrderReport(id);
        return ResponseEntity.noContent().build();
    }
}

