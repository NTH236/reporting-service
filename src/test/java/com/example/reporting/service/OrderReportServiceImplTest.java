package com.example.reporting.service;

import com.example.reporting.client.OrderServiceClient;
import com.example.reporting.client.ProductServiceClient;
import com.example.reporting.dto.*;
import com.example.reporting.entity.OrderReport;
import com.example.reporting.entity.ProductReport;
import com.example.reporting.exception.OrderNotFoundException;
import com.example.reporting.repository.OrderReportRepository;
import com.example.reporting.service.impl.OrderReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderReportServiceImplTest {

    @Mock
    private OrderReportRepository orderReportRepository;

    @Mock
    private OrderServiceClient orderServiceClient;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private OrderReportServiceImpl orderReportService;

    private String token;

    @BeforeEach
    void setUp() {
        token = "Bearer test-jwt-token";
    }

    @Test
    @DisplayName("Successfully calculate financial report for an order with multiple items")
    void testCreateOrderReport_Success() {
        // Given
        Long orderId = 1L;
        OrderReportRequest request = new OrderReportRequest(orderId);

        // Order has 2 items:
        // Item 1: Product 101, qty=2, unit_price=1500.00
        // Item 2: Product 102, qty=3, unit_price=25.00
        ExternalOrderItemDto item1 = ExternalOrderItemDto.builder()
                .productId(101L)
                .quantity(2)
                .unitPrice(new BigDecimal("1500.00"))
                .build();
        ExternalOrderItemDto item2 = ExternalOrderItemDto.builder()
                .productId(102L)
                .quantity(3)
                .unitPrice(new BigDecimal("25.00"))
                .build();

        ExternalOrderDto orderDto = ExternalOrderDto.builder()
                .id(orderId)
                .items(List.of(item1, item2))
                .build();

        // Product 101 cost_price=1100.00 -> revenue=3000, cost=2200, profit=800
        ExternalProductDto product101 = ExternalProductDto.builder()
                .id(101L)
                .unitPrice(new BigDecimal("1500.00"))
                .costPrice(new BigDecimal("1100.00"))
                .build();

        // Product 102 cost_price=15.00 -> revenue=75, cost=45, profit=30
        ExternalProductDto product102 = ExternalProductDto.builder()
                .id(102L)
                .unitPrice(new BigDecimal("25.00"))
                .costPrice(new BigDecimal("15.00"))
                .build();

        when(orderServiceClient.getOrderById(orderId, token)).thenReturn(orderDto);
        when(orderReportRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(productServiceClient.getProductById(101L, token)).thenReturn(product101);
        when(productServiceClient.getProductById(102L, token)).thenReturn(product102);

        when(orderReportRepository.save(any(OrderReport.class))).thenAnswer(invocation -> {
            OrderReport saved = invocation.getArgument(0);
            saved.setId(10L);
            long pid = 1;
            for (ProductReport pr : saved.getProductReports()) {
                pr.setId(pid++);
            }
            return saved;
        });

        // When
        OrderReportResponse response = orderReportService.createOrderReport(request, token);

        // Then
        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(orderId, response.getOrderId());

        // Total calculations:
        // total_revenue = 3000.00 + 75.00 = 3075.00
        // total_cost = 2200.00 + 45.00 = 2245.00
        // total_profit = 3075.00 - 2245.00 = 830.00
        assertEquals(new BigDecimal("3075.00"), response.getTotalRevenue());
        assertEquals(new BigDecimal("2245.00"), response.getTotalCost());
        assertEquals(new BigDecimal("830.00"), response.getTotalProfit());

        assertEquals(2, response.getProductReports().size());

        ProductReportResponse pr1 = response.getProductReports().get(0);
        assertEquals(101L, pr1.getProductId());
        assertEquals(2, pr1.getTotalSold());
        assertEquals(new BigDecimal("3000.00"), pr1.getRevenue());
        assertEquals(new BigDecimal("2200.00"), pr1.getCost());
        assertEquals(new BigDecimal("800.00"), pr1.getProfit());

        ProductReportResponse pr2 = response.getProductReports().get(1);
        assertEquals(102L, pr2.getProductId());
        assertEquals(3, pr2.getTotalSold());
        assertEquals(new BigDecimal("75.00"), pr2.getRevenue());
        assertEquals(new BigDecimal("45.00"), pr2.getCost());
        assertEquals(new BigDecimal("30.00"), pr2.getProfit());

        verify(orderReportRepository, times(1)).save(any(OrderReport.class));
    }

    @Test
    @DisplayName("Throw OrderNotFoundException when order does not exist in Order Service")
    void testCreateOrderReport_OrderNotFound() {
        Long orderId = 999L;
        OrderReportRequest request = new OrderReportRequest(orderId);

        when(orderServiceClient.getOrderById(orderId, token))
                .thenThrow(new OrderNotFoundException(orderId));

        assertThrows(OrderNotFoundException.class, () -> orderReportService.createOrderReport(request, token));
        verify(orderReportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Throw IllegalArgumentException when order has no items")
    void testCreateOrderReport_EmptyItems() {
        Long orderId = 2L;
        OrderReportRequest request = new OrderReportRequest(orderId);
        ExternalOrderDto emptyOrder = ExternalOrderDto.builder().id(orderId).items(List.of()).build();

        when(orderServiceClient.getOrderById(orderId, token)).thenReturn(emptyOrder);

        assertThrows(IllegalArgumentException.class, () -> orderReportService.createOrderReport(request, token));
        verify(orderReportRepository, never()).save(any());
    }
}

