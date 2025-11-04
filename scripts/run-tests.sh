#!/bin/bash

echo "===================================="
echo "AutoTest Platform 测试执行脚本"
echo "===================================="

echo
echo "开始执行测试套件..."
echo

# 设置项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# 创建测试报告目录
mkdir -p target/test-reports

echo "[1/6] 编译项目..."
mvn clean test-compile -q
if [ $? -ne 0 ]; then
    echo "❌ 项目编译失败"
    exit 1
fi
echo "✅ 项目编译完成"

echo
echo "[2/6] 执行单元测试..."
mvn test -Dtest="**/*Test" -Dmaven.test.failure.ignore=true -q
if [ $? -ne 0 ]; then
    echo "⚠️  单元测试存在失败用例"
else
    echo "✅ 单元测试执行完成"
fi

echo
echo "[3/6] 执行集成测试..."
mvn test -Dtest="**/*IntegrationTest" -Dmaven.test.failure.ignore=true -q
if [ $? -ne 0 ]; then
    echo "⚠️  集成测试存在失败用例"
else
    echo "✅ 集成测试执行完成"
fi

echo
echo "[4/6] 执行端到端测试..."
mvn test -Dtest="**/*E2ETest" -Dmaven.test.failure.ignore=true -q
if [ $? -ne 0 ]; then
    echo "⚠️  端到端测试存在失败用例"
else
    echo "✅ 端到端测试执行完成"
fi

echo
echo "[5/6] 执行性能测试..."
mvn test -Dtest="**/*PerformanceTest" -Dmaven.test.failure.ignore=true -q
if [ $? -ne 0 ]; then
    echo "⚠️  性能测试存在失败用例"
else
    echo "✅ 性能测试执行完成"
fi

echo
echo "[6/6] 生成测试报告..."
mvn surefire-report:report -q
echo "✅ 测试报告生成完成"

echo
echo "===================================="
echo "测试执行完成！"
echo "===================================="
echo
echo "📊 测试报告位置:"
echo "    - HTML报告: target/site/surefire-report.html"
echo "    - XML报告: target/surefire-reports/*.xml"
echo "    - 自定义报告: target/test-reports/"
echo
echo "📈 查看测试结果:"
echo "    1. 打开 target/test-reports/test-report.html"
echo "    2. 或使用 Maven 命令: mvn surefire-report:report-only"
echo

# 检查是否有失败的测试
if [ -n "$(ls target/surefire-reports/TEST-*.xml 2>/dev/null)" ]; then
    echo "🔍 检查测试结果..."
    for report in target/surefire-reports/TEST-*.xml; do
        echo "检查报告: $report"
    done
fi

# 生成测试统计摘要
echo
echo "📊 测试统计摘要:"
echo "================================"

# 统计通过的测试
PASSED=$(grep -c "tests=\"0\" errors=\"0\"" target/surefire-reports/*.xml 2>/dev/null || echo "0")
# 统计失败的测试
FAILED=$(grep -c "errors=\"1\"" target/surefire-reports/*.xml 2>/dev/null || echo "0")
# 统计错误
ERRORS=$(grep -c "errors=\"[2-9]\"" target/surefire-reports/*.xml 2>/dev/null || echo "0")
# 总测试数
TOTAL=$((PASSED + FAILED + ERRORS))

if [ $TOTAL -gt 0 ]; then
    echo "总测试数: $TOTAL"
    echo "通过: $PASSED"
    echo "失败: $FAILED"
    echo "错误: $ERRORS"
    SUCCESS_RATE=$((PASSED * 100 / TOTAL))
    echo "成功率: ${SUCCESS_RATE}%"
else
    echo "未找到测试结果文件"
fi

echo "===================================="

# 如果有失败或错误，设置退出码为1
if [ $FAILED -gt 0 ] || [ $ERRORS -gt 0 ]; then
    exit 1
fi

echo "🎉 所有测试通过！"