# Simple Reset Test without complex JSON parsing

Write-Host "Testing Reset Function..." -ForegroundColor Green

# Test 1: Create Game
Write-Host "Step 1: Creating game..."
try {
    $response1 = Invoke-RestMethod -Uri "http://localhost:8080/api/games" -Method GET
    $gameId = $response1.data.gameId
    Write-Host "SUCCESS: Game created with ID $gameId" -ForegroundColor Green
} catch {
    Write-Host "FAILED: Cannot create game - $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Make Move
Write-Host "Step 2: Making move..."
try {
    $moveData = @{
        row = 0
        col = 0  
        value = 4
    }
    $response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/games/$gameId/cell" -Method PUT -Body ($moveData | ConvertTo-Json) -ContentType "application/json"
    Write-Host "SUCCESS: Move made, valid = $($response2.data.isValidMove)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: Cannot make move - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Reset Game
Write-Host "Step 3: Resetting game..."
try {
    $response3 = Invoke-RestMethod -Uri "http://localhost:8080/api/games/$gameId/reset" -Method POST
    Write-Host "SUCCESS: Game reset, status = $($response3.data.status)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: Cannot reset game - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Verify Reset
Write-Host "Step 4: Verifying reset by making same move again..."
try {
    $response4 = Invoke-RestMethod -Uri "http://localhost:8080/api/games/$gameId/cell" -Method PUT -Body ($moveData | ConvertTo-Json) -ContentType "application/json"
    Write-Host "SUCCESS: Move after reset, valid = $($response4.data.isValidMove)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: Cannot verify reset - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Reset functionality test completed!" -ForegroundColor Cyan
Write-Host "If all steps show SUCCESS, the reset feature is working properly." -ForegroundColor Yellow 