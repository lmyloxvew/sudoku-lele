# 数独乐乐后端功能测试指南

本指南提供了完整的后端功能测试方案，包括自动化测试和手动测试。

## 🧪 测试环境设置

### 1. 运行单元测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=SudokuGridTest
mvn test -Dtest=StrategyTest
mvn test -Dtest=PuzzleImportServiceTest

# 运行集成测试
mvn test -Dtest=GameControllerIntegrationTest
```

### 2. 启动应用

```bash
# 启动Spring Boot应用
mvn spring-boot:run

# 应用将在 http://localhost:8080 启动
```

## 📋 手动测试清单

### 基础功能测试

#### 1. 创建新游戏

**测试用例**: 创建默认游戏
```bash
curl -X GET http://localhost:8080/api/games
```

**预期结果**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "gameId": 0,
    "grid": {...},
    "status": "IN_PROGRESS",
    "moveHistory": [],
    "redoHistory": [],
    "startTime": "2024-01-01T10:00:00"
  }
}
```

#### 2. 从字符串创建游戏

**测试用例**: 导入有效的81位字符串
```bash
curl -X POST http://localhost:8080/api/games/import-string \
-H "Content-Type: application/json" \
-d '{
  "puzzleString": "003020600900305001001806400008102900700000008006708200002609500800203009005010300"
}'
```

**测试用例**: 导入无效字符串
```bash
curl -X POST http://localhost:8080/api/games/import-string \
-H "Content-Type: application/json" \
-d '{
  "puzzleString": "invalid_string"
}'
```

**预期结果**: 应返回错误信息

#### 3. 从URL创建游戏

**测试用例**: 从SudokuWiki URL导入
```bash
curl -X POST http://localhost:8080/api/games/import-url \
-H "Content-Type: application/json" \
-d '{
  "url": "https://www.sudokuwiki.org/sudoku.htm?bd=003020600900305001001806400008102900700000008006708200002609500800203009005010300"
}'
```

### 游戏操作测试

#### 1. 有效移动

**测试用例**: 在空格填入合法数字
```bash
curl -X PUT http://localhost:8080/api/games/0/cell \
-H "Content-Type: application/json" \
-d '{
  "row": 0,
  "col": 0,
  "value": 4
}'
```

**预期结果**: `isValidMove: true`

#### 2. 无效移动

**测试用例**: 填入冲突数字
```bash
curl -X PUT http://localhost:8080/api/games/0/cell \
-H "Content-Type: application/json" \
-d '{
  "row": 0,
  "col": 0,
  "value": 3
}'
```

**预期结果**: `isValidMove: false`

#### 3. 清除数字

**测试用例**: 清除已填入的数字
```bash
curl -X PUT http://localhost:8080/api/games/0/cell \
-H "Content-Type: application/json" \
-d '{
  "row": 0,
  "col": 0,
  "value": 0
}'
```

#### 4. 撤销操作

```bash
curl -X POST http://localhost:8080/api/games/0/undo
```

#### 5. 重做操作

```bash
curl -X POST http://localhost:8080/api/games/0/redo
```

#### 6. 获取提示

```bash
curl -X GET http://localhost:8080/api/games/0/hint
```

**预期结果**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "strategyName": "显性唯一 (Naked Single)",
    "description": "位置 (1,1) 只有一个可能的候选数 5，因此这就是答案。",
    "primaryCells": [{"row": 0, "col": 0}],
    "suggestedMove": {
      "row": 0,
      "col": 0,
      "value": 5
    },
    "eliminations": {}
  }
}
```

## 🔬 深度测试场景

### 1. 游戏状态测试

**测试场景**: 游戏完成检测
1. 创建接近完成的游戏:
```bash
curl -X POST http://localhost:8080/api/games/import-string \
-H "Content-Type: application/json" \
-d '{
  "puzzleString": "483921657967345821251876493548132976729564138136798245372689514814253769695417300"
}'
```

2. 填入最后一个数字:
```bash
curl -X PUT http://localhost:8080/api/games/{gameId}/cell \
-H "Content-Type: application/json" \
-d '{
  "row": 8,
  "col": 8,
  "value": 2
}'
```

**预期结果**: 游戏状态应变为 `SOLVED`

### 2. 策略算法测试

**测试不同难度的谜题**:

**简单谜题** (应该能找到Naked Single):
```
483921657967345821251876493548132976729564138136798245372689514814253769695417300
```

**中等谜题** (需要Hidden Single):
```
003020600900305001001806400008102900700000008006708200002609500800203009005010300
```

**复杂谜题**:
```
800000000003600000070090200050007000000045700000100030001000068008500010090000400
```

### 3. 边界条件测试

#### 无效输入测试
```bash
# 测试不存在的游戏ID
curl -X GET http://localhost:8080/api/games/99999/hint

# 测试无效的移动位置
curl -X PUT http://localhost:8080/api/games/0/cell \
-H "Content-Type: application/json" \
-d '{
  "row": 10,
  "col": 10,
  "value": 1
}'

# 测试修改给定数字
curl -X PUT http://localhost:8080/api/games/0/cell \
-H "Content-Type: application/json" \
-d '{
  "row": 0,
  "col": 2,
  "value": 1
}'
```

### 4. 性能测试

**测试场景**: 提示算法响应时间
```bash
# 使用time命令测试响应时间
time curl -X GET http://localhost:8080/api/games/0/hint
```

**预期结果**: 响应时间应在合理范围内（通常 < 1秒）

## 🛠️ Postman测试集合

### 创建Postman测试

1. **环境变量设置**:
   - `baseUrl`: `http://localhost:8080`
   - `gameId`: `0` (动态更新)

2. **测试集合结构**:

```
数独乐乐API测试
├── 游戏创建
│   ├── 创建默认游戏
│   ├── 从字符串创建游戏
│   └── 从URL创建游戏
├── 游戏操作  
│   ├── 有效移动
│   ├── 无效移动
│   ├── 撤销操作
│   ├── 重做操作
│   └── 获取提示
└── 错误处理
    ├── 无效游戏ID
    ├── 无效移动位置
    └── 无效输入格式
```

3. **测试脚本示例**:

```javascript
// 在Tests标签中添加断言
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has success code", function () {
    const jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(1);
});

pm.test("Game ID exists", function () {
    const jsonData = pm.response.json();
    pm.expect(jsonData.data.gameId).to.exist;
    pm.environment.set("gameId", jsonData.data.gameId);
});
```

## 📊 测试数据集

### 1. 标准测试谜题
```javascript
const testPuzzles = {
  easy: "003020600900305001001806400008102900700000008006708200002609500800203009005010300",
  medium: "800000000003600000070090200050007000000045700000100030001000068008500010090000400",
  hard: "850002400720000009004000000000010700230500090004000000000080070017000000000400030",
  nearComplete: "483921657967345821251876493548132976729564138136798245372689514814253769695417300",
  completed: "483921657967345821251876493548132976729564138136798245372689514814253769695417382",
  invalid: "113020600900305001001806400008102900700000008006708200002609500800203009005010300"
};
```

### 2. URL测试用例
```javascript
const testUrls = {
  valid: "https://www.sudokuwiki.org/sudoku.htm?bd=003020600900305001001806400008102900700000008006708200002609500800203009005010300",
  invalid: "https://www.google.com",
  malformed: "not_a_url"
};
```

## ✅ 测试检查清单

### 功能完整性检查
- [ ] 所有API端点都能正常响应
- [ ] 数独基本逻辑验证正确
- [ ] 提示算法能找到正确答案
- [ ] 游戏状态正确更新
- [ ] 撤销/重做功能正常工作
- [ ] 导入功能支持多种格式

### 错误处理检查
- [ ] 无效输入能正确拒绝
- [ ] 网络错误能适当处理
- [ ] 边界条件测试通过
- [ ] 异常情况不会导致崩溃

### 性能检查
- [ ] 提示算法响应时间合理
- [ ] 大量操作不会导致内存泄漏
- [ ] 候选数计算效率高

## 🐛 常见问题排查

### 1. 编译错误
```bash
# 检查Java版本
java -version

# 清理并重新编译
mvn clean compile
```

### 2. 测试失败
```bash
# 查看详细测试报告
mvn test -X

# 运行特定失败的测试
mvn test -Dtest=FailedTestClass#failedTestMethod
```

### 3. 网络相关测试失败
- URL导入测试可能因网络问题失败，这是正常的
- 可以使用Mock进行隔离测试

### 4. 端口占用
```bash
# 检查端口占用
netstat -an | grep 8080

# 更改端口
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

这个测试指南提供了全面的测试方案，确保您的数独乐乐后端功能稳定可靠。 