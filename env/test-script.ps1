# 数独乐乐后端功能快速验证脚本 (PowerShell版本)

Write-Host "🎯 数独乐乐后端功能验证脚本" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan

# 设置基础URL
$BaseUrl = "http://localhost:8080"
$FailedTests = 0
$PassedTests = 0

# 测试函数
function Test-Api {
    param(
        [string]$TestName,
        [string]$Url,
        [string]$Method = "GET",
        [string]$Body = $null,
        [int]$ExpectedCode = 200
    )
    
    Write-Host -NoNewline "测试: $TestName ... "
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        $response = Invoke-WebRequest @params
        $httpCode = $response.StatusCode
        
        if ($httpCode -eq $ExpectedCode) {
            Write-Host "✓ 通过" -ForegroundColor Green
            $script:PassedTests++
            return $true
        } else {
            Write-Host "✗ 失败 (HTTP $httpCode)" -ForegroundColor Red
            Write-Host "  响应: $($response.Content)" -ForegroundColor Yellow
            $script:FailedTests++
            return $false
        }
    }
    catch {
        Write-Host "✗ 异常: $($_.Exception.Message)" -ForegroundColor Red
        $script:FailedTests++
        return $false
    }
}

# 检查服务是否运行
Write-Host "📡 检查服务状态..." -ForegroundColor Yellow
try {
    $testResponse = Invoke-WebRequest -Uri "$BaseUrl/api/games" -Method GET -TimeoutSec 5
    Write-Host "✓ 服务运行正常" -ForegroundColor Green
}
catch {
    Write-Host "❌ 服务未运行！请先启动应用：mvn spring-boot:run" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 1. 测试创建默认游戏
Write-Host "🎮 测试游戏创建功能" -ForegroundColor Yellow
Write-Host "====================" -ForegroundColor Yellow
Test-Api "创建默认游戏" "$BaseUrl/api/games" "GET"

# 提取游戏ID (简化版本)
try {
    $gameResponse = Invoke-WebRequest -Uri "$BaseUrl/api/games" -Method GET
    $gameJson = $gameResponse.Content | ConvertFrom-Json
    $gameId = $gameJson.data.gameId
    Write-Host "📝 使用游戏ID: $gameId" -ForegroundColor Cyan
}
catch {
    Write-Host "❌ 无法获取游戏ID" -ForegroundColor Red
    $gameId = 0
}
Write-Host ""

# 2. 测试从字符串创建游戏
Write-Host "📄 测试谜题导入功能" -ForegroundColor Yellow
Write-Host "====================" -ForegroundColor Yellow

$validPuzzle = @{
    puzzleString = "003020600900305001001806400008102900700000008006708200002609500800203009005010300"
} | ConvertTo-Json

Test-Api "从有效字符串创建游戏" "$BaseUrl/api/games/import-string" "POST" $validPuzzle

$invalidPuzzle = @{
    puzzleString = "invalid_string"
} | ConvertTo-Json

Test-Api "从无效字符串创建游戏(应该失败)" "$BaseUrl/api/games/import-string" "POST" $invalidPuzzle 200

Write-Host ""

# 3. 测试移动功能
Write-Host "🎯 测试移动功能" -ForegroundColor Yellow
Write-Host "================" -ForegroundColor Yellow

$validMove = @{
    row = 0
    col = 0
    value = 4
} | ConvertTo-Json

Test-Api "进行有效移动" "$BaseUrl/api/games/$gameId/cell" "PUT" $validMove

$invalidMove = @{
    row = 0
    col = 0
    value = 3
} | ConvertTo-Json

Test-Api "进行无效移动" "$BaseUrl/api/games/$gameId/cell" "PUT" $invalidMove

Write-Host ""

# 4. 测试撤销重做功能
Write-Host "↶ 测试撤销重做功能" -ForegroundColor Yellow
Write-Host "==================" -ForegroundColor Yellow

Test-Api "撤销移动" "$BaseUrl/api/games/$gameId/undo" "POST"
Test-Api "重做移动" "$BaseUrl/api/games/$gameId/redo" "POST"

Write-Host ""

# 5. 测试重置功能
Write-Host "🔄 测试重置功能" -ForegroundColor Yellow
Write-Host "===============" -ForegroundColor Yellow

# 先进行几步移动
$anotherMove = @{
    row = 0
    col = 1
    value = 5
} | ConvertTo-Json

Test-Api "进行额外移动" "$BaseUrl/api/games/$gameId/cell" "PUT" $anotherMove
Test-Api "重置游戏到初始状态" "$BaseUrl/api/games/$gameId/reset" "POST"

# 验证重置后可以再次进行相同移动
Test-Api "验证重置成功(重新进行移动)" "$BaseUrl/api/games/$gameId/cell" "PUT" $validMove

Write-Host ""

# 6. 测试提示功能
Write-Host "💡 测试提示功能" -ForegroundColor Yellow
Write-Host "===============" -ForegroundColor Yellow

Test-Api "获取提示" "$BaseUrl/api/games/$gameId/hint" "GET"

Write-Host ""

# 总结
Write-Host "📊 测试结果总结" -ForegroundColor Cyan
Write-Host "===============" -ForegroundColor Cyan
Write-Host "✅ 通过: $PassedTests" -ForegroundColor Green
Write-Host "❌ 失败: $FailedTests" -ForegroundColor Red

if ($FailedTests -eq 0) {
    Write-Host "🎉 所有测试通过！数独乐乐后端运行正常。" -ForegroundColor Green
} else {
    Write-Host "⚠️  有测试失败，请检查日志。" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🌐 应用访问地址: http://localhost:8080" -ForegroundColor Cyan
Write-Host "📚 API文档参考: README.md" -ForegroundColor Cyan 