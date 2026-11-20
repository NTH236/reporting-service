package com.example.reporting.service;

import com.example.reporting.dto.OrderReportRequest;
import com.example.reporting.dto.OrderReportResponse;

import java.util.List;

public interface OrderReportService {

    OrderReportResponse createOrderReport(OrderReportRequest request, String bearerToken);

    OrderReportResponse getOrderReportById(Long id);

    List<OrderReportResponse> getAllOrderReports();

    void deleteOrderReport(Long id);
}

