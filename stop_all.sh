#!/usr/bin/env bash

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}===============================================================================${NC}"
echo -e "${YELLOW}               DỪNG TOÀN BỘ CÁC DỊCH VỤ SOA ĐANG CHẠY                          ${NC}"
echo -e "${YELLOW}===============================================================================${NC}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="$SCRIPT_DIR/.running_pids"

# 1. Kill recorded PIDs
if [ -f "$PID_FILE" ]; then
    while IFS= read -r pid; do
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            echo -e "  -> Đang dừng tiến trình PID: ${YELLOW}$pid${NC}..."
            kill "$pid" 2>/dev/null || true
        fi
    done < "$PID_FILE"
    rm -f "$PID_FILE"
fi

# 2. Check and free ports 8081, 8082, 8083, 8084
for port in 8081 8082 8083 8084; do
    pids=$(lsof -ti tcp:"$port" 2>/dev/null || fuser "$port/tcp" 2>/dev/null || true)
    if [ -n "$pids" ]; then
        echo -e "  -> Dọn dẹp tiến trình chiếm giữ cổng ${RED}$port${NC} (PIDs: $pids)..."
        kill -9 $pids 2>/dev/null || true
    fi
done

echo -e "${GREEN}Tất cả các dịch vụ (Port 8081, 8082, 8083, 8084) đã được dừng hoàn tất!${NC}\n"

