@echo off
chcp 65001 > nul
echo ===============================================================================
echo     KHỞI CHẠY HỆ THỐNG SOA BÀI THỰC HÀNH SỐ 5 (REPORTING SERVICE)
echo ===============================================================================
echo.

REM 1. Kiểm tra môi trường Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [LỖI] Không tìm thấy Java trong PATH. Vui lòng cài đặt JDK 21+!
    pause
    exit /b 1
)

REM 2. Kiểm tra và Build các JAR nếu chưa tồn tại
if not exist "target\reporting-service-1.0.0.jar" (
    echo [THÔNG BÁO] Đang build Reporting Service...
    call mvn clean package -DskipTests
)

if not exist "mock-support\target\mock-services-1.0.0.jar" (
    echo [THÔNG BÁO] Đang build Mock Services...
    cd mock-support
    call mvn clean package -DskipTests
    cd ..
)

echo.
echo [1/4] Khởi động Auth Service (Port 8081)...
start "Auth Service (Port 8081)" cmd /k "title Auth Service (8081) && java -jar mock-support\target\mock-services-1.0.0.jar --server.port=8081 --spring.profiles.active=auth"

echo [2/4] Khởi động Product Service (Port 8082)...
start "Product Service (Port 8082)" cmd /k "title Product Service (8082) && java -jar mock-support\target\mock-services-1.0.0.jar --server.port=8082 --spring.profiles.active=product"

echo [3/4] Khởi động Order Service (Port 8083)...
start "Order Service (Port 8083)" cmd /k "title Order Service (8083) && java -jar mock-support\target\mock-services-1.0.0.jar --server.port=8083 --spring.profiles.active=order"

echo Đang chờ các dịch vụ phụ trợ khởi động (5 giây)...
timeout /t 5 /nobreak > nul

echo [4/4] Khởi động Reporting Service (Port 8084)...
start "Reporting Service (Port 8084)" cmd /k "title Reporting Service (8084) && java -jar target\reporting-service-1.0.0.jar --server.port=8084"

echo.
echo ===============================================================================
echo     TẤT CẢ 4 DỊCH VỤ SOA ĐÃ ĐƯỢC MỞ TRÊN 4 CỬA SỔ TERMINAL RIÊNG BIỆT!
echo ===============================================================================
echo  1. Auth Service      : http://localhost:8081/health
echo  2. Product Service   : http://localhost:8082/products
echo  3. Order Service     : http://localhost:8083/orders
echo  4. Reporting Service : http://localhost:8084/reports/orders
echo     - Swagger UI      : http://localhost:8084/swagger-ui.html
echo     - H2 Console      : http://localhost:8084/h2-console
echo ===============================================================================
echo.
echo Nhấn phím bất kỳ để đóng cửa sổ điều phối (các cửa sổ service vẫn chạy bình thường).
pause > nul

