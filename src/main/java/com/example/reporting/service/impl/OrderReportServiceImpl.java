package com.example.reporting.service.impl;

import com.example.reporting.client.OrderServiceClient;
import com.example.reporting.client.ProductServiceClient;
import com.example.reporting.dto.*;
import com.example.reporting.entity.OrderReport;
import com.example.reporting.entity.ProductReport;
import com.example.reporting.exception.OrderNotFoundException;
import com.example.reporting.exception.ResourceNotFoundException;
import com.example.reporting.repository.OrderReportRepository;
import com.example.reporting.service.OrderReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderReportServiceImpl implements OrderReportService {

    private final OrderReportRepository orderReportRepository;
    private final OrderServiceClient orderServiceClient;
    private final ProductServiceClient productServiceClient;

    @Override
    @Transactional
    public OrderReportResponse createOrderReport(OrderReportRequest request, String bearerToken) {
        Long orderId = request.getOrderId();
        log.info("Generating order report for orderId: {}", orderId);

        // 1. Fetch order details from Order Service
        ExternalOrderDto order = orderServiceClient.getOrderById(orderId, bearerToken);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order with ID " + orderId + " has no items to analyze");
        }

        // 2. Lookup existing order report or initialize new
        OrderReport orderReport = orderReportRepository.findByOrderId(orderId)
                .orElse(OrderReport.builder()
                        .orderId(orderId)
                        .productReports(new ArrayList<>())
                        .build());

        // Clear existing product reports if regenerating
        orderReport.getProductReports().clear();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        // 3. Process each item in order
        for (ExternalOrderItemDto item : order.getItems()) {
            Long productId = item.getProductId();
            int quantity = (item.getQuantity() != null && item.getQuantity() > 0) ? item.getQuantity() : 1;

            // Fetch product pricing from Product Service
            ExternalProductDto product = productServiceClient.getProductById(productId, bearerToken);

            BigDecimal unitPrice = (item.getUnitPrice() != null)
                    ? item.getUnitPrice()
                    : (product != null && product.getUnitPrice() != null ? product.getUnitPrice() : BigDecimal.ZERO);

            BigDecimal costPrice = (product != null && product.getCostPrice() != null)
                    ? product.getCostPrice()
                    : BigDecimal.ZERO;

            // item_revenue = quantity * unit_price
            BigDecimal itemRevenue = BigDecimal.valueOf(quantity).multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);

            // item_cost = quantity * cost_price
            BigDecimal itemCost = BigDecimal.valueOf(quantity).multiply(costPrice).setScale(2, RoundingMode.HALF_UP);

            // item_profit = item_revenue - item_cost
            BigDecimal itemProfit = itemRevenue.subtract(itemCost).setScale(2, RoundingMode.HALF_UP);

            ProductReport productReport = ProductReport.builder()
                    .productId(productId)
                    .totalSold(quantity)
                    .revenue(itemRevenue)
                    .cost(itemCost)
                    .profit(itemProfit)
                    .createdAt(LocalDateTime.now())
                    .build();

            orderReport.addProductReport(productReport);

            totalRevenue = totalRevenue.add(itemRevenue);
            totalCost = totalCost.add(itemCost);
        }

        // 4. Summarize for order
        BigDecimal totalProfit = totalRevenue.subtract(totalCost).setScale(2, RoundingMode.HALF_UP);

        orderReport.setTotalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP));
        orderReport.setTotalCost(totalCost.setScale(2, RoundingMode.HALF_UP));
        orderReport.setTotalProfit(totalProfit);
        orderReport.setCreatedAt(LocalDateTime.now());

        OrderReport savedReport = orderReportRepository.save(orderReport);
        log.info("Successfully generated OrderReport ID: {} for orderId: {} [Revenue={}, Cost={}, Profit={}]",
                savedReport.getId(), orderId, savedReport.getTotalRevenue(), savedReport.getTotalCost(), savedReport.getTotalProfit());

        return mapToResponse(savedReport);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderReportResponse getOrderReportById(Long id) {
        OrderReport report = orderReportRepository.findByIdWithProducts(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order report not found with ID: " + id));
        return mapToResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderReportResponse> getAllOrderReports() {
        return orderReportRepository.findAllWithProducts().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteOrderReport(Long id) {
        OrderReport report = orderReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order report not found with ID: " + id));
        orderReportRepository.delete(report);
        log.info("Deleted OrderReport ID: {}", id);
    }

    private OrderReportResponse mapToResponse(OrderReport entity) {
        List<ProductReportResponse> productResponses = entity.getProductReports().stream()
                .map(pr -> ProductReportResponse.builder()
                        .id(pr.getId())
                        .orderReportId(entity.getId())
                        .productId(pr.getProductId())
                        .totalSold(pr.getTotalSold())
                        .revenue(pr.getRevenue())
                        .cost(pr.getCost())
                        .profit(pr.getProfit())
                        .createdAt(pr.getCreatedAt())
                        .build())
                .toList();

        return OrderReportResponse.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .totalRevenue(entity.getTotalRevenue())
                .totalCost(entity.getTotalCost())
                .totalProfit(entity.getTotalProfit())
                .createdAt(entity.getCreatedAt())
                .productReports(productResponses)
                .build();
    }
}

