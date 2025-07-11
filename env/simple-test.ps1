# 简单的reset功能测试脚本

$BaseUrl = "http://localhost:8080"

Write-Host "测试reset功能..." -ForegroundColor Yellow

# 1. 创建游戏
Write-Host "1. 创建游戏..."
$gameResponse = Invoke-WebRequest -Uri "$BaseUrl/api/games" -Method GET
$gameJson = $gameResponse.Content | ConvertFrom-Json
$gameId = $gameJson.data.gameId
Write-Host "游戏ID: $gameId"

# 2. 进行移动
Write-Host "2. 进行移动..."
$move = @{
    row = 0
    col = 0
    value = 4
} | ConvertTo-Json

$moveResponse = Invoke-WebRequest -Uri "$BaseUrl/api/games/$gameId/cell" -Method PUT -ContentType "application/json" -Body $move
Write-Host "移动结果: $($moveResponse.StatusCode)"

# 3. 重置游戏
Write-Host "3. 重置游戏..."
$resetResponse = Invoke-WebRequest -Uri "$BaseUrl/api/games/$gameId/reset" -Method POST
Write-Host "重置结果: $($resetResponse.StatusCode)"

# 4. 验证重置成功 - 再次进行相同移动
Write-Host "4. 验证重置成功..."
$verifyResponse = Invoke-WebRequest -Uri "$BaseUrl/api/games/$gameId/cell" -Method PUT -ContentType "application/json" -Body $move
Write-Host "验证结果: $($verifyResponse.StatusCode)"

Write-Host "reset功能测试完成!" -ForegroundColor Green 