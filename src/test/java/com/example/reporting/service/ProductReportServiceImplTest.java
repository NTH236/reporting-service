package com.example.reporting.service;

import com.example.reporting.client.ProductServiceClient;
import com.example.reporting.dto.ExternalProductDto;
import com.example.reporting.dto.ProductReportRequest;
import com.example.reporting.dto.ProductReportResponse;
import com.example.reporting.entity.OrderReport;
import com.example.reporting.entity.ProductReport;
import com.example.reporting.exception.ResourceNotFoundException;
import com.example.reporting.repository.OrderReportRepository;
import com.example.reporting.repository.ProductReportRepository;
import com.example.reporting.service.impl.ProductReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductReportServiceImplTest {

    @Mock
    private ProductReportRepository productReportRepository;

    @Mock
    private OrderReportRepository orderReportRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private ProductReportServiceImpl productReportService;

    private String token;

    @BeforeEach
    void setUp() {
        token = "Bearer test-jwt-token";
    }

    @Test
    @DisplayName("Create product report fetching price from Product Service")
    void testCreateProductReport_Success() {
        Long productId = 101L;
        ProductReportRequest request = ProductReportRequest.builder()
                .productId(productId)
                .totalSold(4)
                .build();

        ExternalProductDto productDto = ExternalProductDto.builder()
                .id(productId)
                .name("Keyboard")
                .unitPrice(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("60.00"))
                .build();

        when(productServiceClient.getProductById(productId, token)).thenReturn(productDto);
        when(productReportRepository.save(any(ProductReport.class))).thenAnswer(inv -> {
            ProductReport p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductReportResponse response = productReportService.createProductReport(request, token);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(productId, response.getProductId());
        assertEquals(4, response.getTotalSold());
        // revenue = 4 * 100.00 = 400.00
        // cost = 4 * 60.00 = 240.00
        // profit = 400.00 - 240.00 = 160.00
        assertEquals(new BigDecimal("400.00"), response.getRevenue());
        assertEquals(new BigDecimal("240.00"), response.getCost());
        assertEquals(new BigDecimal("160.00"), response.getProfit());
    }

    @Test
    @DisplayName("Filter product reports by productId and orderReportId")
    void testGetAllProductReports_Filters() {
        when(productReportRepository.findByProductIdAndOrderReport_Id(101L, 1L))
                .thenReturn(List.of(
                        ProductReport.builder().id(1L).productId(101L).totalSold(2).revenue(new BigDecimal("100.00")).cost(new BigDecimal("60.00")).profit(new BigDecimal("40.00")).build()
                ));

        List<ProductReportResponse> results = productReportService.getAllProductReports(101L, 1L);
        assertEquals(1, results.size());
        assertEquals(101L, results.get(0).getProductId());
    }

    @Test
    @DisplayName("Delete product report successfully")
    void testDeleteProductReport() {
        ProductReport report = ProductReport.builder().id(5L).productId(101L).build();
        when(productReportRepository.findById(5L)).thenReturn(Optional.of(report));

        productReportService.deleteProductReport(5L);

        verify(productReportRepository, times(1)).delete(report);
    }

    @Test
    @DisplayName("Delete product report throws ResourceNotFoundException if non-existent")
    void testDeleteProductReport_NotFound() {
        when(productReportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productReportService.deleteProductReport(999L));
    }
}

