# 阶段 A-00：创建库与用户（需本机 MySQL 8 且 root 可登录）
# 用法：.\backend\scripts\setup-mysql.ps1
#       .\backend\scripts\setup-mysql.ps1 -RootPassword "你的root密码"

param(
    [string]$RootPassword = ""
)

$initSql = Join-Path $PSScriptRoot "init-mysql.sql"
if (-not (Test-Path $initSql)) {
    Write-Error "找不到 init-mysql.sql: $initSql"
    exit 1
}

$mysqlArgs = @("-u", "root")
if ($RootPassword) {
    $mysqlArgs += "-p$RootPassword"
}

Write-Host "执行 init-mysql.sql ..."
Get-Content $initSql -Raw | & mysql @mysqlArgs
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "若 root 需密码，请运行："
    Write-Host "  .\backend\scripts\setup-mysql.ps1 -RootPassword `"你的密码`""
    Write-Host "或手动在 MySQL Workbench 中打开并执行 backend/scripts/init-mysql.sql"
    exit $LASTEXITCODE
}

Write-Host "完成。可验证："
Write-Host "  mysql -u classdemo -pclassdemo123 classdemo_network -e `"SHOW TABLES;`""
