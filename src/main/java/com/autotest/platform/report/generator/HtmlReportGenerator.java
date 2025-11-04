package com.autotest.platform.report.generator;

import com.autotest.platform.domain.report.TestReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * HTML报告生成器
 *
 * @author autotest
 * @date 2024-01-01
 */
@Slf4j
@Component
public class HtmlReportGenerator {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String REPORT_BASE_DIR = System.getProperty("java.io.tmpdir") + "/autotest-reports";

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 生成HTML执行报告
     */
    public String generateExecutionReport(TestReport testReport) throws ReportGenerationException {
        try {
            // 创建报告目录
            createReportDirectory();

            // 生成报告文件路径
            String reportPath = generateReportPath(testReport);

            // 解析报告数据
            TestReport.ReportStatistics statistics = testReport.getStatistics();
            TestReport.ReportConfig config = testReport.getReportConfig();

            // 生成HTML内容
            String htmlContent = generateHtmlContent(testReport, statistics, config);

            // 写入文件
            Files.write(Paths.get(reportPath), htmlContent.getBytes("UTF-8"));

            // 生成图表数据JSON文件
            generateChartDataFile(testReport);

            log.info("HTML execution report generated: {}", reportPath);
            return reportPath;

        } catch (Exception e) {
            log.error("Failed to generate HTML execution report", e);
            throw new ReportGenerationException("Failed to generate HTML report: " + e.getMessage());
        }
    }

    /**
     * 生成HTML趋势报告
     */
    public String generateTrendReport(TestReport testReport) throws ReportGenerationException {
        try {
            createReportDirectory();
            String reportPath = generateReportPath(testReport);

            TestReport.ReportStatistics statistics = testReport.getStatistics();
            String htmlContent = generateTrendHtmlContent(testReport, statistics);

            Files.write(Paths.get(reportPath), htmlContent.getBytes("UTF-8"));

            log.info("HTML trend report generated: {}", reportPath);
            return reportPath;

        } catch (Exception e) {
            log.error("Failed to generate HTML trend report", e);
            throw new ReportGenerationException("Failed to generate trend report: " + e.getMessage());
        }
    }

    /**
     * 生成HTML摘要报告
     */
    public String generateSummaryReport(TestReport testReport) throws ReportGenerationException {
        try {
            createReportDirectory();
            String reportPath = generateReportPath(testReport);

            TestReport.ReportStatistics statistics = testReport.getStatistics();
            String htmlContent = generateSummaryHtmlContent(testReport, statistics);

            Files.write(Paths.get(reportPath), htmlContent.getBytes("UTF-8"));

            log.info("HTML summary report generated: {}", reportPath);
            return reportPath;

        } catch (Exception e) {
            log.error("Failed to generate HTML summary report", e);
            throw new ReportGenerationException("Failed to generate summary report: " + e.getMessage());
        }
    }

    /**
     * 生成HTML内容
     */
    private String generateHtmlContent(TestReport testReport, TestReport.ReportStatistics statistics,
                                       TestReport.ReportConfig config) {
        StringBuilder html = new StringBuilder();

        // HTML头部
        html.append(generateHtmlHeader(testReport, config));

        // 报告主体
        html.append("<body>");
        html.append(generateReportHeader(testReport));
        html.append(generateExecutiveSummary(statistics));
        html.append(generateExecutionOverview(statistics));

        if (config != null && config.getIncludeCharts()) {
            html.append(generateChartsSection(statistics));
        }

        html.append(generateDetailedResults(testReport, statistics));

        if (config != null && config.getIncludePerformanceMetrics()) {
            html.append(generatePerformanceSection(statistics));
        }

        html.append(generateFooter());
        html.append("</body>");

        // HTML尾部
        html.append("</html>");

        return html.toString();
    }

    /**
     * 生成HTML头部
     */
    private String generateHtmlHeader(TestReport testReport, TestReport.ReportConfig config) {
        StringBuilder header = new StringBuilder();
        header.append("<!DOCTYPE html>\n");
        header.append("<html lang=\"zh-CN\">\n");
        header.append("<head>\n");
        header.append("    <meta charset=\"UTF-8\">\n");
        header.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        header.append("    <title>").append(testReport.getReportName()).append("</title>\n");

        // CSS样式
        header.append(generateReportStyles(config));

        // JavaScript
        header.append(generateReportScripts());

        header.append("</head>\n");
        return header.toString();
    }

    /**
     * 生成CSS样式
     */
    private String generateReportStyles(TestReport.ReportConfig config) {
        StringBuilder styles = new StringBuilder();
        styles.append("    <style>\n");
        styles.append(getDefaultStyles());

        if (config != null && config.getCustomStyles() != null) {
            styles.append(config.getCustomStyles());
        }

        styles.append("    </style>\n");
        return styles.toString();
    }

    /**
     * 获取默认样式
     */
    private String getDefaultStyles() {
        return """
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }

            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                line-height: 1.6;
                color: #333;
                background-color: #f5f5f5;
            }

            .container {
                max-width: 1200px;
                margin: 0 auto;
                padding: 20px;
            }

            .header {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                padding: 40px 20px;
                border-radius: 10px;
                margin-bottom: 30px;
                box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            }

            .header h1 {
                font-size: 2.5em;
                margin-bottom: 10px;
            }

            .header .meta {
                opacity: 0.9;
                font-size: 1.1em;
            }

            .summary-cards {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                gap: 20px;
                margin-bottom: 30px;
            }

            .card {
                background: white;
                padding: 25px;
                border-radius: 10px;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                transition: transform 0.2s ease;
            }

            .card:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
            }

            .card h3 {
                color: #666;
                font-size: 0.9em;
                text-transform: uppercase;
                margin-bottom: 10px;
                letter-spacing: 0.5px;
            }

            .card .value {
                font-size: 2.5em;
                font-weight: bold;
                color: #333;
            }

            .card.success .value {
                color: #28a745;
            }

            .card.danger .value {
                color: #dc3545;
            }

            .card.warning .value {
                color: #ffc107;
            }

            .card.info .value {
                color: #17a2b8;
            }

            .section {
                background: white;
                margin-bottom: 30px;
                border-radius: 10px;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                overflow: hidden;
            }

            .section-header {
                background: #f8f9fa;
                padding: 20px 25px;
                border-bottom: 1px solid #dee2e6;
            }

            .section-header h2 {
                color: #495057;
                font-size: 1.5em;
            }

            .section-content {
                padding: 25px;
            }

            .chart-container {
                height: 400px;
                margin-bottom: 30px;
            }

            .table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 20px;
            }

            .table th,
            .table td {
                padding: 12px;
                text-align: left;
                border-bottom: 1px solid #dee2e6;
            }

            .table th {
                background-color: #f8f9fa;
                font-weight: 600;
                color: #495057;
            }

            .table tbody tr:hover {
                background-color: #f8f9fa;
            }

            .status-badge {
                padding: 4px 8px;
                border-radius: 4px;
                font-size: 0.85em;
                font-weight: 500;
            }

            .status-success {
                background-color: #d4edda;
                color: #155724;
            }

            .status-danger {
                background-color: #f8d7da;
                color: #721c24;
            }

            .status-warning {
                background-color: #fff3cd;
                color: #856404;
            }

            .progress-bar {
                width: 100%;
                height: 20px;
                background-color: #e9ecef;
                border-radius: 10px;
                overflow: hidden;
            }

            .progress-fill {
                height: 100%;
                background: linear-gradient(90deg, #28a745, #20c997);
                transition: width 0.3s ease;
            }

            .footer {
                text-align: center;
                padding: 30px;
                color: #666;
                font-size: 0.9em;
            }

            @media (max-width: 768px) {
                .container {
                    padding: 10px;
                }

                .header h1 {
                    font-size: 2em;
                }

                .summary-cards {
                    grid-template-columns: 1fr;
                }

                .section-content {
                    padding: 15px;
                }
            }
            """;
    }

    /**
     * 生成JavaScript脚本
     */
    private String generateReportScripts() {
        return """
            <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
            <script>
                document.addEventListener('DOMContentLoaded', function() {
                    // 初始化图表
                    initializeCharts();

                    // 添加交互效果
                    addInteractions();
                });

                function initializeCharts() {
                    // 成功率饼图
                    const successCtx = document.getElementById('successChart');
                    if (successCtx) {
                        new Chart(successCtx, {
                            type: 'pie',
                            data: {
                                labels: ['成功', '失败', '跳过'],
                                datasets: [{
                                    data: [
                                        document.getElementById('successCount').value,
                                        document.getElementById('failedCount').value,
                                        document.getElementById('skippedCount').value
                                    ],
                                    backgroundColor: ['#28a745', '#dc3545', '#ffc107']
                                }]
                            },
                            options: {
                                responsive: true,
                                maintainAspectRatio: false
                            }
                        });
                    }

                    // 趋势线图
                    const trendCtx = document.getElementById('trendChart');
                    if (trendCtx) {
                        const trendData = JSON.parse(document.getElementById('trendData').value);
                        new Chart(trendCtx, {
                            type: 'line',
                            data: {
                                labels: trendData.labels,
                                datasets: [{
                                    label: '成功率',
                                    data: trendData.successRates,
                                    borderColor: '#28a745',
                                    backgroundColor: 'rgba(40, 167, 69, 0.1)',
                                    tension: 0.4
                                }]
                            },
                            options: {
                                responsive: true,
                                maintainAspectRatio: false,
                                scales: {
                                    y: {
                                        beginAtZero: true,
                                        max: 100
                                    }
                                }
                            }
                        });
                    }
                }

                function addInteractions() {
                    // 表格行点击展开详情
                    const tableRows = document.querySelectorAll('.expandable-row');
                    tableRows.forEach(row => {
                        row.addEventListener('click', function() {
                            const detailRow = this.nextElementSibling;
                            if (detailRow && detailRow.classList.contains('detail-row')) {
                                detailRow.style.display = detailRow.style.display === 'none' ? 'table-row' : 'none';
                            }
                        });
                    });

                    // 返回顶部按钮
                    const backToTop = document.createElement('button');
                    backToTop.innerHTML = '↑';
                    backToTop.style.cssText = `
                        position: fixed;
                        bottom: 30px;
                        right: 30px;
                        width: 50px;
                        height: 50px;
                        border-radius: 50%;
                        background: #667eea;
                        color: white;
                        border: none;
                        font-size: 20px;
                        cursor: pointer;
                        display: none;
                        z-index: 1000;
                    `;

                    backToTop.addEventListener('click', () => {
                        window.scrollTo({ top: 0, behavior: 'smooth' });
                    });

                    document.body.appendChild(backToTop);

                    window.addEventListener('scroll', () => {
                        backToTop.style.display = window.scrollY > 300 ? 'block' : 'none';
                    });
                }
            </script>
            """;
    }

    /**
     * 生成报告头部
     */
    private String generateReportHeader(TestReport testReport) {
        StringBuilder header = new StringBuilder();
        header.append("    <div class=\"header\">\n");
        header.append("        <div class=\"container\">\n");
        header.append("            <h1>").append(testReport.getReportName()).append("</h1>\n");
        header.append("            <div class=\"meta\">\n");
        header.append("                <p>执行编号: ").append(testReport.getExecutionCode()).append("</p>\n");
        header.append("                <p>生成时间: ").append(DATE_FORMAT.format(new Date())).append("</p>\n");
        header.append("                <p>报告格式: HTML | 页面浏览</p>\n");
        header.append("            </div>\n");
        header.append("        </div>\n");
        header.append("    </div>\n");
        return header.toString();
    }

    /**
     * 生成执行摘要
     */
    private String generateExecutiveSummary(TestReport.ReportStatistics statistics) {
        StringBuilder summary = new StringBuilder();
        summary.append("    <div class=\"container\">\n");
        summary.append("        <div class=\"summary-cards\">\n");

        // 成功率卡片
        summary.append("            <div class=\"card success\">\n");
        summary.append("                <h3>成功率</h3>\n");
        summary.append("                <div class=\"value\">").append(String.format("%.1f%%", statistics.getSuccessRate() * 100)).append("</div>\n");
        summary.append("            </div>\n");

        // 总用例数卡片
        summary.append("            <div class=\"card info\">\n");
        summary.append("                <h3>总用例数</h3>\n");
        summary.append("                <div class=\"value\">").append(statistics.getTotalCases()).append("</div>\n");
        summary.append("            </div>\n");

        // 成功用例数卡片
        summary.append("            <div class=\"card success\">\n");
        summary.append("                <h3>成功用例</h3>\n");
        summary.append("                <div class=\"value\">").append(statistics.getSuccessCases()).append("</div>\n");
        summary.append("            </div>\n");

        // 失败用例数卡片
        summary.append("            <div class=\"card danger\">\n");
        summary.append("                <h3>失败用例</h3>\n");
        summary.append("                <div class=\"value\">").append(statistics.getFailedCases()).append("</div>\n");
        summary.append("            </div>\n");

        summary.append("        </div>\n");
        summary.append("    </div>\n");
        return summary.toString();
    }

    /**
     * 生成执行概览
     */
    private String generateExecutionOverview(TestReport.ReportStatistics statistics) {
        StringBuilder overview = new StringBuilder();
        overview.append("    <div class=\"container\">\n");
        overview.append("        <div class=\"section\">\n");
        overview.append("            <div class=\"section-header\">\n");
        overview.append("                <h2>执行概览</h2>\n");
        overview.append("            </div>\n");
        overview.append("            <div class=\"section-content\">\n");
        overview.append("                <table class=\"table\">\n");
        overview.append("                    <tr>\n");
        overview.append("                        <th>总执行数</th>\n");
        overview.append("                        <td>").append(statistics.getTotalExecutions()).append("</td>\n");
        overview.append("                    </tr>\n");
        overview.append("                    <tr>\n");
        overview.append("                        <th>成功执行数</th>\n");
        overview.append("                        <td>").append(statistics.getSuccessExecutions()).append("</td>\n");
        overview.append("                    </tr>\n");
        overview.append("                    <tr>\n");
        overview.append("                        <th>失败执行数</th>\n");
        overview.append("                        <td>").append(statistics.getFailedExecutions()).append("</td>\n");
        overview.append("                    </tr>\n");
        overview.append("                    <tr>\n");
        overview.append("                        <th>平均执行时间</th>\n");
        overview.append("                        <td>").append(String.format("%.2f秒", statistics.getAvgExecutionTime() / 1000.0)).append("</td>\n");
        overview.append("                    </tr>\n");
        overview.append("                </table>\n");
        overview.append("            </div>\n");
        overview.append("        </div>\n");
        overview.append("    </div>\n");
        return overview.toString();
    }

    /**
     * 生成图表部分
     */
    private String generateChartsSection(TestReport.ReportStatistics statistics) {
        StringBuilder charts = new StringBuilder();
        charts.append("    <div class=\"container\">\n");
        charts.append("        <div class=\"section\">\n");
        charts.append("            <div class=\"section-header\">\n");
        charts.append("                <h2>数据图表</h2>\n");
        charts.append("            </div>\n");
        charts.append("            <div class=\"section-content\">\n");

        // 隐藏数据用于JavaScript
        charts.append("                <input type=\"hidden\" id=\"successCount\" value=\"").append(statistics.getSuccessCases()).append("\">\n");
        charts.append("                <input type=\"hidden\" id=\"failedCount\" value=\"").append(statistics.getFailedCases()).append("\">\n");
        charts.append("                <input type=\"hidden\" id=\"skippedCount\" value=\"").append(statistics.getSkippedCases()).append("\">\n");

        if (statistics.getTrendData() != null && !statistics.getTrendData().isEmpty()) {
            List<String> labels = new ArrayList<>();
            List<Double> successRates = new ArrayList<>();

            for (TestReport.TrendData trend : statistics.getTrendData()) {
                labels.add(trend.getDate());
                successRates.add(trend.getSuccessRate() * 100);
            }

            try {
                charts.append("                <input type=\"hidden\" id=\"trendData\" value=\"")
                     .append(objectMapper.writeValueAsString(Map.of("labels", labels, "successRates", successRates)))
                     .append("\">\n");
            } catch (Exception e) {
                log.warn("Failed to serialize trend data", e);
            }
        }

        charts.append("                <div class=\"row\">\n");
        charts.append("                    <div class=\"col-md-6\">\n");
        charts.append("                        <h3>执行结果分布</h3>\n");
        charts.append("                        <div class=\"chart-container\">\n");
        charts.append("                            <canvas id=\"successChart\"></canvas>\n");
        charts.append("                        </div>\n");
        charts.append("                    </div>\n");
        charts.append("                    <div class=\"col-md-6\">\n");
        charts.append("                        <h3>成功率趋势</h3>\n");
        charts.append("                        <div class=\"chart-container\">\n");
        charts.append("                            <canvas id=\"trendChart\"></canvas>\n");
        charts.append("                        </div>\n");
        charts.append("                    </div>\n");
        charts.append("                </div>\n");
        charts.append("            </div>\n");
        charts.append("        </div>\n");
        charts.append("    </div>\n");
        return charts.toString();
    }

    /**
     * 生成详细结果
     */
    private String generateDetailedResults(TestReport testReport, TestReport.ReportStatistics statistics) {
        StringBuilder results = new StringBuilder();
        results.append("    <div class=\"container\">\n");
        results.append("        <div class=\"section\">\n");
        results.append("            <div class=\"section-header\">\n");
        results.append("                <h2>详细结果</h2>\n");
        results.append("            </div>\n");
        results.append("            <div class=\"section-content\">\n");

        if (statistics.getTopFailedCases() != null && !statistics.getTopFailedCases().isEmpty()) {
            results.append("                <h3>热门失败用例</h3>\n");
            results.append("                <table class=\"table\">\n");
            results.append("                    <thead>\n");
            results.append("                        <tr>\n");
            results.append("                            <th>用例标题</th>\n");
            results.append("                            <th>用例类型</th>\n");
            results.append("                            <th>失败次数</th>\n");
            results.append("                            <th>失败率</th>\n");
            results.append("                            <th>最后失败时间</th>\n");
            results.append("                        </tr>\n");
            results.append("                    </thead>\n");
            results.append("                    <tbody>\n");

            for (TestReport.FailedCaseInfo failedCase : statistics.getTopFailedCases()) {
                results.append("                        <tr>\n");
                results.append("                            <td>").append(failedCase.getCaseTitle()).append("</td>\n");
                results.append("                            <td>").append(failedCase.getCaseType()).append("</td>\n");
                results.append("                            <td>").append(failedCase.getFailureCount()).append("</td>\n");
                results.append("                            <td>").append(String.format("%.1f%%", failedCase.getFailureRate() * 100)).append("</td>\n");
                results.append("                            <td>").append(failedCase.getLastFailureTime()).append("</td>\n");
                results.append("                        </tr>\n");
            }

            results.append("                    </tbody>\n");
            results.append("                </table>\n");
        }

        results.append("            </div>\n");
        results.append("        </div>\n");
        results.append("    </div>\n");
        return results.toString();
    }

    /**
     * 生成性能部分
     */
    private String generatePerformanceSection(TestReport.ReportStatistics statistics) {
        StringBuilder performance = new StringBuilder();
        performance.append("    <div class=\"container\">\n");
        performance.append("        <div class=\"section\">\n");
        performance.append("            <div class=\"section-header\">\n");
        performance.append("                <h2>性能指标</h2>\n");
        performance.append("            </div>\n");
        performance.append("            <div class=\"section-content\">\n");
        performance.append("                <table class=\"table\">\n");
        performance.append("                    <tr>\n");
        performance.append("                        <th>平均执行时间</th>\n");
        performance.append("                        <td>").append(String.format("%.2f秒", statistics.getAvgExecutionTime() / 1000.0)).append("</td>\n");
        performance.append("                    </tr>\n");
        performance.append("                    <tr>\n");
        performance.append("                        <th>最长执行时间</th>\n");
        performance.append("                        <td>").append(String.format("%.2f秒", statistics.getMaxExecutionTime() / 1000.0)).append("</td>\n");
        performance.append("                    </tr>\n");
        performance.append("                    <tr>\n");
        performance.append("                        <th>最短执行时间</th>\n");
        performance.append("                        <td>").append(String.format("%.2f秒", statistics.getMinExecutionTime() / 1000.0)).append("</td>\n");
        performance.append("                    </tr>\n");
        performance.append("                </table>\n");
        performance.append("            </div>\n");
        performance.append("        </div>\n");
        performance.append("    </div>\n");
        return performance.toString();
    }

    /**
     * 生成页脚
     */
    private String generateFooter() {
        StringBuilder footer = new StringBuilder();
        footer.append("    <div class=\"footer\">\n");
        footer.append("        <div class=\"container\">\n");
        footer.append("            <p>本报告由 AutoTest Platform 自动生成</p>\n");
        footer.append("            <p>生成时间: ").append(DATE_FORMAT.format(new Date())).append("</p>\n");
        footer.append("        </div>\n");
        footer.append("    </div>\n");
        return footer.toString();
    }

    /**
     * 生成趋势报告HTML内容
     */
    private String generateTrendHtmlContent(TestReport testReport, TestReport.ReportStatistics statistics) {
        // 趋势报告的专门HTML生成逻辑
        return generateHtmlContent(testReport, statistics, testReport.getReportConfig());
    }

    /**
     * 生成摘要报告HTML内容
     */
    private String generateSummaryHtmlContent(TestReport testReport, TestReport.ReportStatistics statistics) {
        // 摘要报告的专门HTML生成逻辑
        return generateHtmlContent(testReport, statistics, testReport.getReportConfig());
    }

    /**
     * 创建报告目录
     */
    private void createReportDirectory() throws IOException {
        Path reportDir = Paths.get(REPORT_BASE_DIR);
        if (!Files.exists(reportDir)) {
            Files.createDirectories(reportDir);
        }
    }

    /**
     * 生成报告文件路径
     */
    private String generateReportPath(TestReport testReport) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        return String.format("%s/%s_%s_%s.html",
            REPORT_BASE_DIR, testReport.getReportCode(), testReport.getReportType(), timestamp);
    }

    /**
     * 生成图表数据文件
     */
    private void generateChartDataFile(TestReport testReport) {
        try {
            if (testReport.getChartData() != null && !testReport.getChartData().isEmpty()) {
                String chartDataPath = generateReportPath(testReport).replace(".html", "_charts.json");
                String chartDataJson = objectMapper.writeValueAsString(testReport.getChartData());
                Files.write(Paths.get(chartDataPath), chartDataJson.getBytes("UTF-8"));
            }
        } catch (Exception e) {
            log.warn("Failed to generate chart data file", e);
        }
    }

    /**
     * 报告生成异常
     */
    public static class ReportGenerationException extends Exception {
        public ReportGenerationException(String message) {
            super(message);
        }

        public ReportGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}