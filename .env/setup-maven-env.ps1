# Maven Environment Setup Script - 支持自定义安装路径
# 可以安装在任何盘符，推荐F盘

Write-Host "Maven Environment Setup Script" -ForegroundColor Green
Write-Host "==============================" -ForegroundColor Green

# Java路径
$javaHome = "F:\MINECRAFT"

# 自动检测Maven可能的安装位置
$possibleMavenPaths = @(
    "F:\tools\maven\apache-maven-3.9.9",
    "F:\software\maven\apache-maven-3.9.9", 
    "F:\maven\apache-maven-3.9.9",
    "F:\tools\apache-maven-3.9.9",
    "F:\software\apache-maven-3.9.9",
    "F:\apache-maven-3.9.9",
    "C:\tools\maven\apache-maven-3.9.9",
    "C:\maven\apache-maven-3.9.9",
    "C:\tools\apache-maven-3.9.9",
    "C:\apache-maven-3.9.9",
    "C:\Program Files\Maven\apache-maven-3.9.9",
    "C:\Program Files (x86)\Maven\apache-maven-3.9.9"
)

Write-Host "Searching for Maven installation..." -ForegroundColor Yellow

$mavenHome = $null
foreach ($path in $possibleMavenPaths) {
    if (Test-Path "$path\bin\mvn.cmd") {
        $mavenHome = $path
        Write-Host "✅ Found Maven at: $mavenHome" -ForegroundColor Green
        break
    }
}

if ($mavenHome -eq $null) {
    Write-Host "❌ Maven not found in any expected location!" -ForegroundColor Red
    Write-Host ""
    Write-Host "请告诉我你的Maven安装在哪里？" -ForegroundColor Yellow
    Write-Host "常见位置:" -ForegroundColor Cyan
    foreach ($path in $possibleMavenPaths) {
        Write-Host "   - $path" -ForegroundColor White
    }
    
    $customPath = Read-Host "`n请输入Maven安装目录的完整路径（例如: F:\maven\apache-maven-3.9.9）"
    
    if ($customPath -and (Test-Path "$customPath\bin\mvn.cmd")) {
        $mavenHome = $customPath
        Write-Host "✅ Found Maven at: $mavenHome" -ForegroundColor Green
    } else {
        Write-Host "❌ 无法在指定路径找到Maven，请检查路径是否正确" -ForegroundColor Red
        Read-Host "`n按任意键退出"
        exit 1
    }
}

# 检查Java
if (-not (Test-Path "$javaHome\bin\java.exe")) {
    Write-Host "⚠️  Warning: Java not found at $javaHome" -ForegroundColor Yellow
    Write-Host "Please verify your Java installation path" -ForegroundColor Yellow
}

# 设置环境变量
Write-Host "`nSetting environment variables..." -ForegroundColor Yellow

[Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, "User")
[Environment]::SetEnvironmentVariable("MAVEN_HOME", $mavenHome, "User")

# 更新PATH
$userPath = [Environment]::GetEnvironmentVariable("PATH", "User")
$mavenBin = "$mavenHome\bin"

if ($userPath -eq $null) { $userPath = "" }

if ($userPath -notlike "*$mavenBin*") {
    if ($userPath -eq "") {
        $newPath = $mavenBin
    } else {
        $newPath = "$userPath;$mavenBin"
    }
    [Environment]::SetEnvironmentVariable("PATH", $newPath, "User")
    Write-Host "✅ Added Maven to PATH" -ForegroundColor Green
} else {
    Write-Host "✅ Maven already in PATH" -ForegroundColor Green
}

# 为当前会话设置环境变量
$env:JAVA_HOME = $javaHome
$env:MAVEN_HOME = $mavenHome
$env:PATH = "$env:PATH;$mavenBin"

Write-Host "`n✅ Environment variables configured:" -ForegroundColor Green
Write-Host "   JAVA_HOME  = $javaHome" -ForegroundColor Cyan
Write-Host "   MAVEN_HOME = $mavenHome" -ForegroundColor Cyan

# 测试Maven
Write-Host "`nTesting Maven..." -ForegroundColor Yellow
try {
    $versionOutput = & "$mavenBin\mvn.cmd" --version 2>&1
    Write-Host $versionOutput -ForegroundColor White
    Write-Host "`n🎉 Maven安装成功！可以运行 'mvn test' 了" -ForegroundColor Green
} catch {
    Write-Host "`n❌ Maven测试失败，可能需要重启PowerShell" -ForegroundColor Yellow
}

Write-Host "`n📝 注意: 建议重启PowerShell使环境变量完全生效" -ForegroundColor Yellow 