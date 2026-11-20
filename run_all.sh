#!/usr/bin/env bash
set -e

# Colors for terminal output
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${CYAN}===============================================================================${NC}"
echo -e "${CYAN}      KHỞI CHẠY HỆ THỐNG SOA BÀI THỰC HÀNH SỐ 5 (REPORTING SERVICE)           ${NC}"
echo -e "${CYAN}===============================================================================${NC}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

mkdir -p logs
PID_FILE="$SCRIPT_DIR/.running_pids"
> "$PID_FILE"

# 1. Build jars if not present
if [ ! -f "target/reporting-service-1.0.0.jar" ]; then
    echo -e "${YELLOW}[THÔNG BÁO] Đang build Reporting Service...${NC}"
    mvn clean package -DskipTests
fi

if [ ! -f "mock-support/target/mock-services-1.0.0.jar" ]; then
    echo -e "${YELLOW}[THÔNG BÁO] Đang build Mock Services...${NC}"
    (cd mock-support && mvn clean package -DskipTests)
fi

# Function to check if a port is ready
wait_for_port() {
    local port=$1
    local name=$2
    local max_retries=30
    local count=0

    echo -ne "  -> Đang chờ ${name} (Port ${port}) sẵn sàng..."
    while ! nc -z localhost "$port" 2>/dev/null && ! curl -s "http://localhost:${port}" >/dev/null 2>&1; do
        sleep 1
        count=$((count + 1))
        if [ "$count" -ge "$max_retries" ]; then
            echo -e " ${RED}[TIMEOUT]${NC}"
            echo -e "${RED}[LỖI] Dịch vụ ${name} không thể khởi động sau ${max_retries}s. Kiểm tra log!${NC}"
            return 1
        fi
        echo -ne "."
    done
    echo -e " ${GREEN}[OK]${NC}"
    return 0
}

# 2. Khởi chạy Auth Service (Port 8081)
echo -e "\n${YELLOW}[1/4] Khởi động Auth Service (Port 8081)...${NC}"
nohup java -jar mock-support/target/mock-services-1.0.0.jar --server.port=8081 --spring.profiles.active=auth > logs/auth-service.log 2>&1 &
AUTH_PID=$!
echo "$AUTH_PID" >> "$PID_FILE"
echo "  -> PID: $AUTH_PID | Log: logs/auth-service.log"

# 3. Khởi chạy Product Service (Port 8082)
echo -e "\n${YELLOW}[2/4] Khởi động Product Service (Port 8082)...${NC}"
nohup java -jar mock-support/target/mock-services-1.0.0.jar --server.port=8082 --spring.profiles.active=product > logs/product-service.log 2>&1 &
PRODUCT_PID=$!
echo "$PRODUCT_PID" >> "$PID_FILE"
echo "  -> PID: $PRODUCT_PID | Log: logs/product-service.log"

# 4. Khởi chạy Order Service (Port 8083)
echo -e "\n${YELLOW}[3/4] Khởi động Order Service (Port 8083)...${NC}"
nohup java -jar mock-support/target/mock-services-1.0.0.jar --server.port=8083 --spring.profiles.active=order > logs/order-service.log 2>&1 &
ORDER_PID=$!
echo "$ORDER_PID" >> "$PID_FILE"
echo "  -> PID: $ORDER_PID | Log: logs/order-service.log"

# 5. Khởi chạy Reporting Service (Port 8084)
echo -e "\n${YELLOW}[4/4] Khởi động Reporting Service (Port 8084)...${NC}"
nohup java -jar target/reporting-service-1.0.0.jar --server.port=8084 > logs/reporting-service.log 2>&1 &
REPORTING_PID=$!
echo "$REPORTING_PID" >> "$PID_FILE"
echo "  -> PID: $REPORTING_PID | Log: logs/reporting-service.log"

# 6. Chờ tất cả các dịch vụ HTTP sẵn sàng
echo -e "\n${CYAN}Kiểm tra trạng thái sẵn sàng (Health Check)...${NC}"
wait_for_port 8081 "Auth Service"
wait_for_port 8082 "Product Service"
wait_for_port 8083 "Order Service"
wait_for_port 8084 "Reporting Service"

echo -e "\n${GREEN}===============================================================================${NC}"
echo -e "${GREEN}      TẤT CẢ 4 DỊCH VỤ SOA ĐÃ KHỞI CHẠY VÀ SẴN SÀNG PHỤC VỤ!                 ${NC}"
echo -e "${GREEN}===============================================================================${NC}"
echo -e "  * Auth Service      : ${CYAN}http://localhost:8081/health${NC}"
echo -e "  * Product Service   : ${CYAN}http://localhost:8082/products${NC}"
echo -e "  * Order Service     : ${CYAN}http://localhost:8083/orders${NC}"
echo -e "  * Reporting Service : ${CYAN}http://localhost:8084/reports/orders${NC}"
echo -e "    - Swagger UI Docs : ${CYAN}http://localhost:8084/swagger-ui.html${NC}"
echo -e "    - OpenAPI JSON    : ${CYAN}http://localhost:8084/v3/api-docs${NC}"
echo -e "    - H2 Console DB   : ${CYAN}http://localhost:8084/h2-console${NC}"
echo -e "${GREEN}===============================================================================${NC}"
echo -e "Để chạy kịch bản kiểm thử tự động, gõ:  ${YELLOW}./test_reporting_api.sh${NC}"
echo -e "Để dừng tất cả các dịch vụ đang chạy, gõ: ${YELLOW}./stop_all.sh${NC}\n"

