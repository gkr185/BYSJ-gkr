@echo off
chcp 65001 >nul
echo ====================================
echo 社区团购系统 - 微服务数据库创建
echo ====================================
echo.
echo 正在创建数据库，请稍候...
echo.

cd /d %~dp0

echo [1/7] 创建用户服务数据库...
mysql -u root -p123456 < 01_user_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 用户服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 用户服务数据库创建成功
echo.

echo [2/7] 创建商品服务数据库...
mysql -u root -p123456 < 02_product_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 商品服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 商品服务数据库创建成功
echo.

echo [3/7] 创建拼团服务数据库...
mysql -u root -p123456 < 03_groupbuy_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 拼团服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 拼团服务数据库创建成功
echo.

echo [4/7] 创建订单服务数据库...
mysql -u root -p123456 < 04_order_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 订单服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 订单服务数据库创建成功
echo.

echo [5/7] 创建支付服务数据库...
mysql -u root -p123456 < 05_payment_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 支付服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 支付服务数据库创建成功
echo.

echo [6/7] 创建配送服务数据库...
mysql -u root -p123456 < 06_delivery_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 配送服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 配送服务数据库创建成功
echo.

echo [7/7] 创建团长服务数据库...
mysql -u root -p123456 < 07_leader_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 团长服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 团长服务数据库创建成功
echo.

echo ====================================
echo ✅ 所有数据库创建完成！
echo ====================================
echo.
echo 已创建7个数据库：
echo   - user_service_db      (5张表)
echo   - product_service_db   (2张表)
echo   - groupbuy_service_db  (3张表)
echo   - order_service_db     (3张表)
echo   - payment_service_db   (1张表)
echo   - delivery_service_db  (1张表)
echo   - leader_service_db    (4张表)
echo.
echo 总计19张表，已导入原有数据
echo.
echo 下一步：修改各微服务的application.yml配置
echo.
pause

