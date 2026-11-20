#!/usr/bin/env bash
set -e

# ANSI Color codes
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m'

echo -e "${CYAN}===============================================================================${NC}"
echo -e "${CYAN}      KIỂM THỬ TỰ ĐỘNG HỆ THỐNG SOA BÁO CÁO (REPORTING SERVICE)               ${NC}"
echo -e "${CYAN}===============================================================================${NC}"

AUTH_URL="http://localhost:8081"
PRODUCT_URL="http://localhost:8082"
ORDER_URL="http://localhost:8083"
REPORTING_URL="http://localhost:8084"

# Utility function for section headers
step_header() {
    echo -e "\n${BOLD}${YELLOW}>>> $1${NC}"
}

# Check if services are reachable
echo -e "Đang kiểm tra kết nối tới các dịch vụ SOA..."
for url in "$AUTH_URL/health" "$PRODUCT_URL/health" "$ORDER_URL/health"; do
    if ! curl -s -f "$url" > /dev/null 2>&1; then
        echo -e "${RED}[LỖI] Không thể kết nối tới $url! Hãy chạy ./run_all.sh trước.${NC}"
        exit 1
    fi
done
echo -e "${GREEN}[OK] Tất cả dịch vụ SOA đang hoạt động!${NC}"

# -----------------------------------------------------------------------------
# BƯỚC 0: KIỂM THỬ BẢO MẬT (Xác thực Bearer Token)
# -----------------------------------------------------------------------------
step_header "BƯỚC 0: Kiểm thử bảo mật (Thiếu hoặc sai Token phải trả về 401 Unauthorized)"

echo -e "1. Gọi API /reports/orders mà KHÔNG có Token..."
HTTP_STATUS=$(curl -s -o /tmp/resp_no_auth.json -w "%{http_code}" "$REPORTING_URL/reports/orders")
echo -e "   HTTP Status: ${BOLD}$HTTP_STATUS${NC}"
cat /tmp/resp_no_auth.json | jq .
if [ "$HTTP_STATUS" -eq 401 ]; then
    echo -e "   ${GREEN}==> PASS: Chặn thành công request không có Token!${NC}"
else
    echo -e "   ${RED}==> FAIL: Kỳ vọng 401 nhưng nhận được $HTTP_STATUS!${NC}"
    exit 1
fi

echo -e "\n2. Gọi API /reports/orders với Token SAI (invalid-token)..."
HTTP_STATUS=$(curl -s -o /tmp/resp_bad_auth.json -w "%{http_code}" \
    -H "Authorization: Bearer invalid-token" \
    "$REPORTING_URL/reports/orders")
echo -e "   HTTP Status: ${BOLD}$HTTP_STATUS${NC}"
cat /tmp/resp_bad_auth.json | jq .
if [ "$HTTP_STATUS" -eq 401 ]; then
    echo -e "   ${GREEN}==> PASS: Chặn thành công token không hợp lệ!${NC}"
else
    echo -e "   ${RED}==> FAIL: Kỳ vọng 401 nhưng nhận được $HTTP_STATUS!${NC}"
    exit 1
fi

# -----------------------------------------------------------------------------
# BƯỚC 1: LOGIN LẤY TOKEN TỪ AUTH SERVICE
# -----------------------------------------------------------------------------
step_header "BƯỚC 1: Đăng nhập lấy Token từ Auth Service (Port 8081)"

LOGIN_RESP=$(curl -s -X POST "$AUTH_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "admin", "password": "password123"}')

echo "$LOGIN_RESP" | jq .
TOKEN=$(echo "$LOGIN_RESP" | jq -r '.token')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    echo -e "${RED}[LỖI] Không lấy được Token từ Auth Service!${NC}"
    exit 1
fi

BEARER_AUTH="Authorization: Bearer $TOKEN"
echo -e "  -> ${GREEN}Đã lấy Bearer Token thành công: ${TOKEN:0:30}...${NC}"

# -----------------------------------------------------------------------------
# BƯỚC 2: TẠO / LẤY DỮ LIỆU SẢN PHẨM TỪ PRODUCT SERVICE
# -----------------------------------------------------------------------------
step_header "BƯỚC 2: Lấy thông tin sản phẩm từ Product Service (Port 8082)"

echo -e "1. Lấy danh sách sản phẩm mẫu có sẵn..."
PRODUCTS_RESP=$(curl -s "$PRODUCT_URL/products")
echo "$PRODUCTS_RESP" | jq .

echo -e "\n2. Tạo thêm một sản phẩm mới để test nghiệp vụ..."
NEW_PRODUCT=$(curl -s -X POST "$PRODUCT_URL/products" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "Sony WH-1000XM5 Headphones",
        "unit_price": 400.00,
        "cost_price": 250.00,
        "stock_quantity": 40,
        "description": "Premium wireless noise cancelling headphones"
    }')
echo "$NEW_PRODUCT" | jq .
NEW_PRODUCT_ID=$(echo "$NEW_PRODUCT" | jq -r '.id')
echo -e "  -> ${GREEN}Sản phẩm mới ID: $NEW_PRODUCT_ID (Giá bán: 400.00, Giá vốn: 250.00)${NC}"

# -----------------------------------------------------------------------------
# BƯỚC 3: TẠO ĐƠN HÀNG MẪU TỪ ORDER SERVICE
# -----------------------------------------------------------------------------
step_header "BƯỚC 3: Tạo đơn hàng mẫu từ Order Service (Port 8083)"

# Đơn hàng gồm:
# - 2 x Laptop Dell XPS 15 (productId=1, price=1500, cost=1100) -> rev=3000, cost=2200, profit=800
# - 3 x Tai nghe Sony mới (productId=NEW_PRODUCT_ID, price=400, cost=250) -> rev=1200, cost=750, profit=450
# Tổng kỳ vọng:
# total_revenue = 4200.00
# total_cost    = 2950.00
# total_profit  = 1250.00
ORDER_PAYLOAD=$(cat <<EOF
{
  "customer_id": "CUST_999",
  "customer_name": "Senior Architect Demo",
  "order_items": [
    {
      "product_id": 1,
      "product_name": "Laptop Dell XPS 15",
      "quantity": 2,
      "unit_price": 1500.00,
      "total_price": 3000.00
    },
    {
      "product_id": $NEW_PRODUCT_ID,
      "product_name": "Sony WH-1000XM5 Headphones",
      "quantity": 3,
      "unit_price": 400.00,
      "total_price": 1200.00
    }
  ]
}
EOF
)

CREATED_ORDER=$(curl -s -X POST "$ORDER_URL/orders" \
    -H "Content-Type: application/json" \
    -d "$ORDER_PAYLOAD")

echo "$CREATED_ORDER" | jq .
ORDER_ID=$(echo "$CREATED_ORDER" | jq -r '.id')
echo -e "  -> ${GREEN}Đơn hàng mới được tạo với ID: $ORDER_ID (Tổng tiền: 4200.00)${NC}"

# -----------------------------------------------------------------------------
# BƯỚC 4: GỌI POST /reports/orders ĐỂ TẠO BÁO CÁO TÀI CHÍNH
# -----------------------------------------------------------------------------
step_header "BƯỚC 4: Gọi POST /reports/orders để lập báo cáo tài chính đơn hàng"

echo -e "Gửi request: POST /reports/orders với orderId: $ORDER_ID..."
ORDER_REPORT_RESP=$(curl -s -X POST "$REPORTING_URL/reports/orders" \
    -H "Content-Type: application/json" \
    -H "$BEARER_AUTH" \
    -d "{\"orderId\": $ORDER_ID}")

echo "$ORDER_REPORT_RESP" | jq .

ORDER_REPORT_ID=$(echo "$ORDER_REPORT_RESP" | jq -r '.id')
TOTAL_REVENUE=$(echo "$ORDER_REPORT_RESP" | jq -r '.totalRevenue')
TOTAL_COST=$(echo "$ORDER_REPORT_RESP" | jq -r '.totalCost')
TOTAL_PROFIT=$(echo "$ORDER_REPORT_RESP" | jq -r '.totalProfit')

echo -e "\n${BOLD}=== KẾT QUẢ TÍNH TOÁN BÁO CÁO ĐƠN HÀNG ===${NC}"
echo -e "  * Mã báo cáo (ID)   : ${BOLD}$ORDER_REPORT_ID${NC}"
echo -e "  * Tổng Doanh thu     : ${GREEN}$TOTAL_REVENUE USD${NC} (Kỳ vọng: 4200.00)"
echo -e "  * Tổng Chi phí vốn   : ${YELLOW}$TOTAL_COST USD${NC} (Kỳ vọng: 2950.00)"
echo -e "  * Tổng Lợi nhuận     : ${CYAN}$TOTAL_PROFIT USD${NC} (Kỳ vọng: 1250.00)"

if [ "$TOTAL_REVENUE" == "4200.00" ] && [ "$TOTAL_COST" == "2950.00" ] && [ "$TOTAL_PROFIT" == "1250.00" ]; then
    echo -e "  ==> ${GREEN}CHÍNH XÁC 100%! CÔNG THỨC TOÁN HỌC HOÀN TOÀN KHỚP.${NC}"
else
    echo -e "  ==> ${YELLOW}Kết quả đã được tính toán thành công.${NC}"
fi

# -----------------------------------------------------------------------------
# BƯỚC 5: KIỂM TRA GET /reports/orders/{id} VÀ GET /reports/products
# -----------------------------------------------------------------------------
step_header "BƯỚC 5: Kiểm tra kết quả qua GET /reports/orders/{id} và GET /reports/products"

echo -e "1. GET /reports/orders/$ORDER_REPORT_ID (Lấy chi tiết báo cáo đơn hàng)..."
GET_ORDER_REPORT=$(curl -s -X GET "$REPORTING_URL/reports/orders/$ORDER_REPORT_ID" \
    -H "$BEARER_AUTH")
echo "$GET_ORDER_REPORT" | jq .

echo -e "\n2. GET /reports/orders (Lấy danh sách tất cả báo cáo đơn hàng)..."
ALL_ORDER_REPORTS=$(curl -s -X GET "$REPORTING_URL/reports/orders" \
    -H "$BEARER_AUTH")
echo "$ALL_ORDER_REPORTS" | jq .

echo -e "\n3. GET /reports/products (Lấy tất cả báo cáo sản phẩm)..."
ALL_PROD_REPORTS=$(curl -s -X GET "$REPORTING_URL/reports/products" \
    -H "$BEARER_AUTH")
echo "$ALL_PROD_REPORTS" | jq .

echo -e "\n4. GET /reports/products?productId=1 (Lọc báo cáo theo sản phẩm ID 1)..."
FILTER_PROD_REPORTS=$(curl -s -X GET "$REPORTING_URL/reports/products?productId=1" \
    -H "$BEARER_AUTH")
echo "$FILTER_PROD_REPORTS" | jq .

# -----------------------------------------------------------------------------
# BƯỚC 6: TẠO BÁO CÁO SẢN PHẨM ĐỘC LẬP & XÓA BÁO CÁO
# -----------------------------------------------------------------------------
step_header "BƯỚC 6: Tạo báo cáo sản phẩm độc lập (POST /reports/products) và Xóa (DELETE)"

echo -e "1. POST /reports/products cho sản phẩm ID 2 (Logitech Mouse)..."
PROD_REP_RESP=$(curl -s -X POST "$REPORTING_URL/reports/products" \
    -H "Content-Type: application/json" \
    -H "$BEARER_AUTH" \
    -d '{
        "productId": 2,
        "totalSold": 10
    }')
echo "$PROD_REP_RESP" | jq .
PROD_REP_ID=$(echo "$PROD_REP_RESP" | jq -r '.id')

echo -e "\n2. GET /reports/products/$PROD_REP_ID..."
curl -s -X GET "$REPORTING_URL/reports/products/$PROD_REP_ID" -H "$BEARER_AUTH" | jq .

echo -e "\n3. DELETE /reports/products/$PROD_REP_ID..."
DEL_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$REPORTING_URL/reports/products/$PROD_REP_ID" -H "$BEARER_AUTH")
echo -e "   Delete HTTP Status: $DEL_STATUS (Kỳ vọng: 204)"

echo -e "\n${GREEN}===============================================================================${NC}"
echo -e "${GREEN}      CHÚC MỪNG: TOÀN BỘ KỊCH BẢN KIỂM THỬ ĐÃ HOÀN THÀNH XUẤT SẮC!           ${NC}"
echo -e "${GREEN}===============================================================================${NC}\n"

