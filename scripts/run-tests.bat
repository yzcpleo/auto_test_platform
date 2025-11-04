@echo off
echo ====================================
echo AutoTest Platform 测试执行脚本
echo ====================================

echo.
echo 开始执行测试套件...
echo.

:: 设置Java环境变量
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.12
set PATH=%JAVA_HOME%\bin;%PATH%

:: 设置项目根目录
set PROJECT_ROOT=%~dp0..
cd /d "%PROJECT_ROOT%"

:: 创建测试报告目录
if not exist "target\test-reports" mkdir "target\test-reports"

echo [1/6] 编译项目...
mvn clean test-compile -q
if %ERRORLEVEL% neq 0 (
    echo ❌ 项目编译失败
    exit /b 1
)
echo ✅ 项目编译完成

echo.
echo [2/6] 执行单元测试...
mvn test -Dtest="**/*Test" -Dmaven.test.failure.ignore=true -q
if %ERRORLEVEL% neq 0 (
    echo ⚠️  单元测试存在失败用例
) else (
    echo ✅ 单元测试执行完成
)

echo.
echo [3/6] 执行集成测试...
mvn test -Dtest="**/*IntegrationTest" -Dmaven.test.failure.ignore=true -q
if %ERRORLEVEL% neq 0 (
    echo ⚠️  集成测试存在失败用例
) else (
    echo ✅ 集成测试执行完成
)

echo.
echo [4/6] 执行端到端测试...
mvn test -Dtest="**/*E2ETest" -Dmaven.test.failure.ignore=true -q
if %ERRORLEVEL% neq 0 (
    echo ⚠️  端到端测试存在失败用例
) else (
    echo ✅ 端到端测试执行完成
)

echo.
echo [5/6] 执行性能测试...
mvn test -Dtest="**/*PerformanceTest" -Dmaven.test.failure.ignore=true -q
if %ERRORLEVEL% neq 0 (
    echo ⚠️  性能测试存在失败用例
) else (
    echo ✅ 性能测试执行完成
)

echo.
echo [6/6] 生成测试报告...
mvn surefire-report:report -q
echo ✅ 测试报告生成完成

echo.
echo ====================================
echo 测试执行完成！
echo ====================================
echo.
echo 📊 测试报告位置:
echo    - HTML报告: target\site\surefire-report.html
echo    - XML报告: target\surefire-reports\*.xml
echo    - 自定义报告: target\test-reports\
echo.
echo 📈 查看测试结果:
echo    1. 打开 target\test-reports\test-report.html
echo    2. 或使用 Maven 命令: mvn surefire-report:report-only
echo.

:: 检查是否有失败的测试
dir /b target\surefire-reports\TEST-*.xml >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo 🔍 检查测试结果...
    for %%f in (target\surefire-reports\TEST-*.xml) do (
        echo 检查报告: %%f
    )
)

echo.
echo 按任意键退出...
pause >nul