package com.example.reporting.service;

import com.example.reporting.dto.ProductReportRequest;
import com.example.reporting.dto.ProductReportResponse;

import java.util.List;

public interface ProductReportService {

    ProductReportResponse createProductReport(ProductReportRequest request, String bearerToken);

    ProductReportResponse getProductReportById(Long id);

    List<ProductReportResponse> getAllProductReports(Long productId, Long orderReportId);

    void deleteProductReport(Long id);
}

