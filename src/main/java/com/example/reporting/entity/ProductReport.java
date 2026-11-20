package com.example.reporting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_report_id")
    private OrderReport orderReport;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "total_sold", nullable = false)
    private Integer totalSold;

    @Column(name = "revenue", nullable = false, precision = 10, scale = 2)
    private BigDecimal revenue;

    @Column(name = "cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "profit", nullable = false, precision = 10, scale = 2)
    private BigDecimal profit;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.revenue == null) {
            this.revenue = BigDecimal.ZERO;
        }
        if (this.cost == null) {
            this.cost = BigDecimal.ZERO;
        }
        if (this.profit == null) {
            this.profit = BigDecimal.ZERO;
        }
        if (this.totalSold == null) {
            this.totalSold = 0;
        }
    }
}

