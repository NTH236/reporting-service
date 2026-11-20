package com.example.reporting.repository;

import com.example.reporting.entity.ProductReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductReportRepository extends JpaRepository<ProductReport, Long> {

    List<ProductReport> findByProductId(Long productId);

    List<ProductReport> findByOrderReport_Id(Long orderReportId);

    List<ProductReport> findByProductIdAndOrderReport_Id(Long productId, Long orderReportId);

    @Query("SELECT COALESCE(SUM(pr.totalSold), 0) FROM ProductReport pr WHERE pr.productId = :productId")
    Integer getTotalSoldByProductId(@Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(pr.revenue), 0) FROM ProductReport pr WHERE pr.productId = :productId")
    BigDecimal getTotalRevenueByProductId(@Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(pr.cost), 0) FROM ProductReport pr WHERE pr.productId = :productId")
    BigDecimal getTotalCostByProductId(@Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(pr.profit), 0) FROM ProductReport pr WHERE pr.productId = :productId")
    BigDecimal getTotalProfitByProductId(@Param("productId") Long productId);
}

