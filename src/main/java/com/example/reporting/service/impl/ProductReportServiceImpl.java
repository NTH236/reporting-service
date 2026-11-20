package com.example.reporting.service.impl;

import com.example.reporting.client.ProductServiceClient;
import com.example.reporting.dto.ExternalProductDto;
import com.example.reporting.dto.ProductReportRequest;
import com.example.reporting.dto.ProductReportResponse;
import com.example.reporting.entity.OrderReport;
import com.example.reporting.entity.ProductReport;
import com.example.reporting.exception.ResourceNotFoundException;
import com.example.reporting.repository.OrderReportRepository;
import com.example.reporting.repository.ProductReportRepository;
import com.example.reporting.service.ProductReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReportServiceImpl implements ProductReportService {

    private final ProductReportRepository productReportRepository;
    private final OrderReportRepository orderReportRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    @Transactional
    public ProductReportResponse createProductReport(ProductReportRequest request, String bearerToken) {
        Long productId = request.getProductId();
        log.info("Creating product report for productId: {}", productId);

        // Fetch product information from Product Service to resolve prices if missing
        ExternalProductDto product = productServiceClient.getProductById(productId, bearerToken);

        BigDecimal unitPrice = request.getUnitPrice() != null
                ? request.getUnitPrice()
                : (product != null && product.getUnitPrice() != null ? product.getUnitPrice() : BigDecimal.ZERO);

        BigDecimal costPrice = request.getCostPrice() != null
                ? request.getCostPrice()
                : (product != null && product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO);

        int quantity = (request.getTotalSold() != null && request.getTotalSold() > 0)
                ? request.getTotalSold()
                : 1;

        BigDecimal revenue = BigDecimal.valueOf(quantity).multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cost = BigDecimal.valueOf(quantity).multiply(costPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal profit = revenue.subtract(cost).setScale(2, RoundingMode.HALF_UP);

        ProductReport report = ProductReport.builder()
                .productId(productId)
                .totalSold(quantity)
                .revenue(revenue)
                .cost(cost)
                .profit(profit)
                .createdAt(LocalDateTime.now())
                .build();

        if (request.getOrderReportId() != null) {
            OrderReport orderReport = orderReportRepository.findById(request.getOrderReportId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order report not found with ID: " + request.getOrderReportId()));
            report.setOrderReport(orderReport);
        }

        ProductReport saved = productReportRepository.save(report);
        log.info("Created ProductReport ID: {} for productId: {} [Revenue={}, Cost={}, Profit={}]",
                saved.getId(), productId, saved.getRevenue(), saved.getCost(), saved.getProfit());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReportResponse getProductReportById(Long id) {
        ProductReport report = productReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product report not found with ID: " + id));
        return mapToResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReportResponse> getAllProductReports(Long productId, Long orderReportId) {
        List<ProductReport> reports;
        if (productId != null && orderReportId != null) {
            reports = productReportRepository.findByProductIdAndOrderReport_Id(productId, orderReportId);
        } else if (productId != null) {
            reports = productReportRepository.findByProductId(productId);
        } else if (orderReportId != null) {
            reports = productReportRepository.findByOrderReport_Id(orderReportId);
        } else {
            reports = productReportRepository.findAll();
        }

        return reports.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public void deleteProductReport(Long id) {
        ProductReport report = productReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product report not found with ID: " + id));
        productReportRepository.delete(report);
        log.info("Deleted ProductReport ID: {}", id);
    }

    private ProductReportResponse mapToResponse(ProductReport entity) {
        return ProductReportResponse.builder()
                .id(entity.getId())
                .orderReportId(entity.getOrderReport() != null ? entity.getOrderReport().getId() : null)
                .productId(entity.getProductId())
                .totalSold(entity.getTotalSold())
                .revenue(entity.getRevenue())
                .cost(entity.getCost())
                .profit(entity.getProfit())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

