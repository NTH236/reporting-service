package com.example.reporting.repository;

import com.example.reporting.entity.OrderReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderReportRepository extends JpaRepository<OrderReport, Long> {

    Optional<OrderReport> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    @Query("SELECT DISTINCT o FROM OrderReport o LEFT JOIN FETCH o.productReports WHERE o.id = :id")
    Optional<OrderReport> findByIdWithProducts(@Param("id") Long id);

    @Query("SELECT DISTINCT o FROM OrderReport o LEFT JOIN FETCH o.productReports")
    List<OrderReport> findAllWithProducts();
}

