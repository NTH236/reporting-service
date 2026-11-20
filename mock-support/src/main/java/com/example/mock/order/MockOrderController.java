package com.example.mock.order;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/orders")
@Profile({"order", "default"})
public class MockOrderController {

    private final Map<Long, Map<String, Object>> orderDatabase = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(10);

    public MockOrderController() {
        seedInitialOrders();
    }

    private void seedInitialOrders() {
        // Order 1: 2 x Laptop (1500) + 3 x Mouse (25) = 3075.00
        Map<String, Object> order1 = new LinkedHashMap<>();
        order1.put("id", 1L);
        order1.put("customer_id", "CUST_001");
        order1.put("customer_name", "Nguyen Van A");
        order1.put("status", "COMPLETED");
        order1.put("total_amount", new BigDecimal("3075.00"));

        List<Map<String, Object>> items1 = List.of(
                Map.of(
                        "id", 101L,
                        "product_id", 1L,
                        "product_name", "Laptop Dell XPS 15",
                        "quantity", 2,
                        "unit_price", new BigDecimal("1500.00"),
                        "total_price", new BigDecimal("3000.00")
                ),
                Map.of(
                        "id", 102L,
                        "product_id", 2L,
                        "product_name", "Logitech MX Master 3S Mouse",
                        "quantity", 3,
                        "unit_price", new BigDecimal("25.00"),
                        "total_price", new BigDecimal("75.00")
                )
        );
        order1.put("order_items", items1);
        orderDatabase.put(1L, order1);

        // Order 2: 4 x Keyboard (80) = 320.00
        Map<String, Object> order2 = new LinkedHashMap<>();
        order2.put("id", 2L);
        order2.put("customer_id", "CUST_002");
        order2.put("customer_name", "Tran Thi B");
        order2.put("status", "PAID");
        order2.put("total_amount", new BigDecimal("320.00"));

        List<Map<String, Object>> items2 = List.of(
                Map.of(
                        "id", 201L,
                        "product_id", 3L,
                        "product_name", "Keychron K2 Mechanical Keyboard",
                        "quantity", 4,
                        "unit_price", new BigDecimal("80.00"),
                        "total_price", new BigDecimal("320.00")
                )
        );
        order2.put("order_items", items2);
        orderDatabase.put(2L, order2);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllOrders() {
        return ResponseEntity.ok(new ArrayList<>(orderDatabase.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable("id") Long id) {
        Map<String, Object> order = orderDatabase.get(id);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", 404,
                    "error", "Not Found",
                    "message", "Order not found with ID: " + id
            ));
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> payload) {
        Long id = idGenerator.incrementAndGet();
        Map<String, Object> order = new LinkedHashMap<>();
        order.put("id", id);
        order.put("customer_id", payload.getOrDefault("customer_id", "CUST_" + id));
        order.put("customer_name", payload.getOrDefault("customer_name", "Customer " + id));
        order.put("status", payload.getOrDefault("status", "CONFIRMED"));

        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.getOrDefault("order_items", new ArrayList<>());
        order.put("order_items", items);

        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : items) {
            BigDecimal price = new BigDecimal(item.getOrDefault("unit_price", "0").toString());
            int qty = Integer.parseInt(item.getOrDefault("quantity", "1").toString());
            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }
        order.put("total_amount", total);

        orderDatabase.put(id, order);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}

