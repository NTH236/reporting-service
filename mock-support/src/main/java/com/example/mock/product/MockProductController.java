package com.example.mock.product;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/products")
@Profile({"product", "default"})
public class MockProductController {

    private final Map<Long, Map<String, Object>> productDatabase = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(10);

    public MockProductController() {
        seedInitialProducts();
    }

    private void seedInitialProducts() {
        productDatabase.put(1L, new LinkedHashMap<>(Map.of(
                "id", 1L,
                "name", "Laptop Dell XPS 15",
                "unit_price", new BigDecimal("1500.00"),
                "cost_price", new BigDecimal("1100.00"),
                "stock_quantity", 50,
                "description", "High-performance ultrabook with 4K OLED display"
        )));

        productDatabase.put(2L, new LinkedHashMap<>(Map.of(
                "id", 2L,
                "name", "Logitech MX Master 3S Mouse",
                "unit_price", new BigDecimal("25.00"),
                "cost_price", new BigDecimal("15.00"),
                "stock_quantity", 200,
                "description", "Ergonomic wireless mouse"
        )));

        productDatabase.put(3L, new LinkedHashMap<>(Map.of(
                "id", 3L,
                "name", "Keychron K2 Mechanical Keyboard",
                "unit_price", new BigDecimal("80.00"),
                "cost_price", new BigDecimal("45.00"),
                "stock_quantity", 120,
                "description", "Wireless mechanical keyboard with Gateron switches"
        )));

        productDatabase.put(4L, new LinkedHashMap<>(Map.of(
                "id", 4L,
                "name", "LG UltraFine 27-inch 4K Monitor",
                "unit_price", new BigDecimal("450.00"),
                "cost_price", new BigDecimal("320.00"),
                "stock_quantity", 30,
                "description", "Color-accurate IPS display with USB-C"
        )));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllProducts() {
        return ResponseEntity.ok(new ArrayList<>(productDatabase.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") Long id) {
        Map<String, Object> product = productDatabase.get(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", 404,
                    "error", "Not Found",
                    "message", "Product not found with ID: " + id
            ));
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody Map<String, Object> payload) {
        Long id = idGenerator.incrementAndGet();
        Map<String, Object> product = new LinkedHashMap<>();
        product.put("id", id);
        product.put("name", payload.getOrDefault("name", "Product " + id));
        product.put("unit_price", new BigDecimal(payload.getOrDefault("unit_price", "100.00").toString()));
        product.put("cost_price", new BigDecimal(payload.getOrDefault("cost_price", "60.00").toString()));
        product.put("stock_quantity", Integer.parseInt(payload.getOrDefault("stock_quantity", "10").toString()));
        product.put("description", payload.getOrDefault("description", ""));

        productDatabase.put(id, product);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
}

