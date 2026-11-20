package com.example.reporting.controller;

import com.example.reporting.dto.ErrorResponse;
import com.example.reporting.dto.ProductReportRequest;
import com.example.reporting.dto.ProductReportResponse;
import com.example.reporting.service.ProductReportService;
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
@RequestMapping("/reports/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Reports", description = "APIs for analyzing and reporting product-level financial metrics")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductReportController {

    private final ProductReportService productReportService;

    @PostMapping
    @Operation(summary = "Create product report", description = "Creates a product report based on data from product and order services.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Report created successfully",
                    content = @Content(schema = @Schema(implementation = ProductReportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductReportResponse> createProductReport(
            @Valid @RequestBody ProductReportRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        log.info("REST request to create product report for productId: {}", request.getProductId());
        ProductReportResponse response = productReportService.createProductReport(request, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all product reports", description = "Retrieves all product reports with optional filtering by productId or orderReportId.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of product reports retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductReportResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ProductReportResponse>> getAllProductReports(
            @Parameter(description = "Optional filter by Product ID") @RequestParam(value = "productId", required = false) Long productId,
            @Parameter(description = "Optional filter by Order Report ID") @RequestParam(value = "orderReportId", required = false) Long orderReportId) {
        log.info("REST request to get product reports [productId={}, orderReportId={}]", productId, orderReportId);
        return ResponseEntity.ok(productReportService.getAllProductReports(productId, orderReportId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product report by ID", description = "Retrieves details of a specific product report.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report found",
                    content = @Content(schema = @Schema(implementation = ProductReportResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Report not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductReportResponse> getProductReportById(
            @Parameter(description = "ID of the product report", example = "1") @PathVariable("id") Long id) {
        log.info("REST request to get product report ID: {}", id);
        return ResponseEntity.ok(productReportService.getProductReportById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product report", description = "Deletes a specific product report.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Report deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Report not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteProductReport(
            @Parameter(description = "ID of the product report to delete", example = "1") @PathVariable("id") Long id) {
        log.info("REST request to delete product report ID: {}", id);
        productReportService.deleteProductReport(id);
        return ResponseEntity.noContent().build();
    }
}

