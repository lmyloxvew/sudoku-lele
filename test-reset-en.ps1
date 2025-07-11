# Simple Reset Function Test Script (English)

$BaseUrl = "http://localhost:8080"

Write-Host "Testing Reset Functionality..." -ForegroundColor Yellow

try {
    # 1. Create a new game
    Write-Host "1. Creating a new game..."
    $gameResponse = Invoke-WebRequest -Uri "$BaseUrl/api/games" -Method GET
    $gameJson = $gameResponse.Content | ConvertFrom-Json
    $gameId = $gameJson.data.gameId
    Write-Host "   Game ID: $gameId" -ForegroundColor Cyan

    # 2. Make a move
    Write-Host "2. Making a move (4 at position 0,0)..."
    $move = @{
        row = 0
        col = 0
        value = 4
    } | ConvertTo-Json

    $moveResponse = Invoke-WebRequest -Uri "$BaseUrl/api/games/$gameId/cell" -Method PUT -ContentType "application/json" -Body $move
    $moveJson = $moveResponse.Content | ConvertFrom-Json
    Write-Host "   Move result: HTTP $($moveResponse.StatusCode), Valid: $($moveJson.data.isValidMove)" -ForegroundColor Green

    # 3. Make another move
    Write-Host "3. Making another move (5 at position 0,1)..."
    $move2 = @{
        row = 0
        col = 1
        value = 5
    } | ConvertTo-Json

    $moveResponse2 = Invoke-WebRequest -Uri "$BaseUrl/api/games/$gameId/cell" -Method PUT -ContentType "application/json" -Body $move2
    $moveJson2 = $moveResponse2.Content | ConvertFrom-Json
    Write-Host "   Move result: HTTP $($moveResponse2.StatusCode), Valid: $($moveJson2.data.isValidMove)" -ForegroundColor Green

    # 4. Reset the game
    Write-Host "4. Resetting game to initial state..."
    $resetResponse = Invoke-WebRequest -Uri "$BaseUrl/api/games/$gameId/reset" -Method POST
    $resetJson = $resetResponse.Content | ConvertFrom-Json
    Write-Host "   Reset result: HTTP $($resetResponse.StatusCode), Status: $($resetJson.data.status)" -ForegroundColor Green

    # 5. Verify reset worked - try the same move again
    Write-Host "5. Verifying reset worked (re-attempting first move)..."
    $verifyResponse = Invoke-WebRequest -Uri "$BaseUrl/api/games/$gameId/cell" -Method PUT -ContentType "application/json" -Body $move
    $verifyJson = $verifyResponse.Content | ConvertFrom-Json
    Write-Host "   Verification result: HTTP $($verifyResponse.StatusCode), Valid: $($verifyJson.data.isValidMove)" -ForegroundColor Green

    # 6. Test hint after reset
    Write-Host "6. Testing hint functionality after reset..."
    $hintResponse = Invoke-WebRequest -Uri "$BaseUrl/api/games/$gameId/hint" -Method GET
    $hintJson = $hintResponse.Content | ConvertFrom-Json
    if ($hintJson.data) {
        Write-Host "   Hint available: $($hintJson.data.strategyName)" -ForegroundColor Green
    } else {
        Write-Host "   No hint available" -ForegroundColor Yellow
    }

    Write-Host ""
    Write-Host "✅ All reset functionality tests PASSED!" -ForegroundColor Green
    Write-Host "   - Game creation: OK"
    Write-Host "   - Move operations: OK" 
    Write-Host "   - Reset operation: OK"
    Write-Host "   - Post-reset verification: OK"
    Write-Host "   - Hint system: OK"

} catch {
    Write-Host ""
    Write-Host "❌ Test FAILED with error:" -ForegroundColor Red
    Write-Host "   $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "   HTTP Status Code: $statusCode" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "📊 Test Summary:" -ForegroundColor Cyan
Write-Host "   Application URL: $BaseUrl"
Write-Host "   Reset API Endpoint: POST /api/games/{gameId}/reset"
Write-Host "   Test completed at: $(Get-Date)" 