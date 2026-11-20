package com.example.reporting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "total_revenue", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "total_profit", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalProfit;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "orderReport", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductReport> productReports = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.totalRevenue == null) {
            this.totalRevenue = BigDecimal.ZERO;
        }
        if (this.totalCost == null) {
            this.totalCost = BigDecimal.ZERO;
        }
        if (this.totalProfit == null) {
            this.totalProfit = BigDecimal.ZERO;
        }
    }

    public void addProductReport(ProductReport productReport) {
        this.productReports.add(productReport);
        productReport.setOrderReport(this);
    }

    public void removeProductReport(ProductReport productReport) {
        this.productReports.remove(productReport);
        productReport.setOrderReport(null);
    }
}

