-- Schema definition for Reporting Service Database
CREATE TABLE IF NOT EXISTS orders_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    total_revenue DECIMAL(10, 2) NOT NULL,
    total_cost DECIMAL(10, 2) NOT NULL,
    total_profit DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_report_id BIGINT,
    product_id BIGINT NOT NULL,
    total_sold INT NOT NULL,
    revenue DECIMAL(10, 2) NOT NULL,
    cost DECIMAL(10, 2) NOT NULL,
    profit DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_report FOREIGN KEY (order_report_id) REFERENCES orders_reports(id) ON DELETE CASCADE
);

