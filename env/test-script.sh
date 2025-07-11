#!/bin/bash

# 数独乐乐后端功能快速验证脚本

echo "🎯 数独乐乐后端功能验证脚本"
echo "=================================="

# 设置基础URL
BASE_URL="http://localhost:8080"
FAILED_TESTS=0
PASSED_TESTS=0

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试函数
test_api() {
    local test_name="$1"
    local url="$2"
    local method="$3"
    local data="$4"
    local expected_code="$5"
    
    echo -n "测试: $test_name ... "
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "%{http_code}" "$url")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" "$url")
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "%{http_code}" -X PUT -H "Content-Type: application/json" -d "$data" "$url")
    fi
    
    http_code="${response: -3}"
    body="${response%???}"
    
    if [ "$http_code" = "$expected_code" ]; then
        echo -e "${GREEN}✓ 通过${NC}"
        ((PASSED_TESTS++))
        return 0
    else
        echo -e "${RED}✗ 失败 (HTTP $http_code)${NC}"
        echo "  响应: $body"
        ((FAILED_TESTS++))
        return 1
    fi
}

# 检查服务是否运行
echo "📡 检查服务状态..."
if ! curl -s "$BASE_URL/api/games" > /dev/null; then
    echo -e "${RED}❌ 服务未运行！请先启动应用：mvn spring-boot:run${NC}"
    exit 1
fi
echo -e "${GREEN}✓ 服务运行正常${NC}"
echo

# 1. 测试创建默认游戏
echo "🎮 测试游戏创建功能"
echo "===================="
test_api "创建默认游戏" "$BASE_URL/api/games" "GET" "" "200"

# 提取游戏ID
GAME_RESPONSE=$(curl -s "$BASE_URL/api/games")
GAME_ID=$(echo "$GAME_RESPONSE" | grep -o '"gameId":[0-9]*' | cut -d':' -f2)
echo "📝 使用游戏ID: $GAME_ID"
echo

# 2. 测试从字符串创建游戏
VALID_PUZZLE='{"puzzleString":"003020600900305001001806400008102900700000008006708200002609500800203009005010300"}'
test_api "从有效字符串创建游戏" "$BASE_URL/api/games/import-string" "POST" "$VALID_PUZZLE" "200"

INVALID_PUZZLE='{"puzzleString":"invalid"}'
test_api "从无效字符串创建游戏" "$BASE_URL/api/games/import-string" "POST" "$INVALID_PUZZLE" "200"

echo

# 3. 测试游戏操作
echo "🎯 测试游戏操作功能"
echo "===================="

# 测试有效移动
VALID_MOVE='{"row":0,"col":0,"value":4}'
test_api "有效移动" "$BASE_URL/api/games/$GAME_ID/cell" "PUT" "$VALID_MOVE" "200"

# 测试无效移动
INVALID_MOVE='{"row":0,"col":0,"value":3}'
test_api "无效移动" "$BASE_URL/api/games/$GAME_ID/cell" "PUT" "$INVALID_MOVE" "200"

# 测试撤销
test_api "撤销操作" "$BASE_URL/api/games/$GAME_ID/undo" "POST" "" "200"

# 测试重做
test_api "重做操作" "$BASE_URL/api/games/$GAME_ID/redo" "POST" "" "200"

# 测试获取提示
test_api "获取提示" "$BASE_URL/api/games/$GAME_ID/hint" "GET" "" "200"

echo

# 4. 测试边界条件
echo "⚠️  测试边界条件"
echo "=================="

# 测试不存在的游戏ID
test_api "不存在的游戏ID" "$BASE_URL/api/games/99999/hint" "GET" "" "200"

# 测试超出范围的移动
OUT_OF_BOUNDS='{"row":10,"col":10,"value":1}'
test_api "越界移动" "$BASE_URL/api/games/$GAME_ID/cell" "PUT" "$OUT_OF_BOUNDS" "200"

echo

# 5. 性能测试
echo "⚡ 性能测试"
echo "============"

echo -n "测试提示算法响应时间... "
start_time=$(date +%s%N)
curl -s "$BASE_URL/api/games/$GAME_ID/hint" > /dev/null
end_time=$(date +%s%N)
duration=$(( (end_time - start_time) / 1000000 )) # 转换为毫秒

if [ $duration -lt 1000 ]; then
    echo -e "${GREEN}✓ 响应时间: ${duration}ms${NC}"
    ((PASSED_TESTS++))
else
    echo -e "${YELLOW}⚠ 响应时间: ${duration}ms (较慢)${NC}"
    ((FAILED_TESTS++))
fi

echo

# 6. 测试游戏完成流程
echo "🏆 测试游戏完成流程"
echo "===================="

# 创建接近完成的游戏
NEAR_COMPLETE='{"puzzleString":"483921657967345821251876493548132976729564138136798245372689514814253769695417300"}'
COMPLETE_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d "$NEAR_COMPLETE" "$BASE_URL/api/games/import-string")
COMPLETE_GAME_ID=$(echo "$COMPLETE_RESPONSE" | grep -o '"gameId":[0-9]*' | cut -d':' -f2)

# 完成最后一步
FINAL_MOVE='{"row":8,"col":8,"value":2}'
echo -n "测试游戏完成检测... "
FINAL_RESPONSE=$(curl -s -X PUT -H "Content-Type: application/json" -d "$FINAL_MOVE" "$BASE_URL/api/games/$COMPLETE_GAME_ID/cell")

if echo "$FINAL_RESPONSE" | grep -q "SOLVED"; then
    echo -e "${GREEN}✓ 游戏完成检测正常${NC}"
    ((PASSED_TESTS++))
else
    echo -e "${RED}✗ 游戏完成检测失败${NC}"
    ((FAILED_TESTS++))
fi

echo

# 7. 测试数独逻辑验证
echo "🧮 测试数独逻辑验证"
echo "===================="

# 创建有冲突的数独测试
CONFLICT_PUZZLE='{"puzzleString":"113020600900305001001806400008102900700000008006708200002609500800203009005010300"}'
echo -n "测试冲突检测... "
CONFLICT_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d "$CONFLICT_PUZZLE" "$BASE_URL/api/games/import-string")

if echo "$CONFLICT_RESPONSE" | grep -q "导入失败"; then
    echo -e "${GREEN}✓ 冲突检测正常${NC}"
    ((PASSED_TESTS++))
else
    echo -e "${YELLOW}⚠ 冲突检测可能有问题${NC}"
    ((FAILED_TESTS++))
fi

echo

# 测试结果汇总
echo "📊 测试结果汇总"
echo "================"
echo -e "通过测试: ${GREEN}$PASSED_TESTS${NC}"
echo -e "失败测试: ${RED}$FAILED_TESTS${NC}"
echo -e "总计测试: $((PASSED_TESTS + FAILED_TESTS))"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n${GREEN}🎉 所有测试通过！后端功能正常！${NC}"
    exit 0
else
    echo -e "\n${YELLOW}⚠️  有 $FAILED_TESTS 个测试失败，请检查相关功能${NC}"
    exit 1
fi 