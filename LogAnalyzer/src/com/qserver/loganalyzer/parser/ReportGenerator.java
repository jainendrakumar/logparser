package com.qserver.loganalyzer.report;

import com.qserver.loganalyzer.analyzer.MemoryAnalyzer;
import com.qserver.loganalyzer.analyzer.PerformanceAnalyzer;
import com.qserver.loganalyzer.analyzer.RootCauseAnalyzer;
import com.qserver.loganalyzer.model.Transaction;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates analysis reports in various formats.
 * This class provides methods for creating detailed reports of transaction analysis results.
 */
public class ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
    
    private final List<Transaction> transactions;
    private final PerformanceAnalyzer performanceAnalyzer;
    private final MemoryAnalyzer memoryAnalyzer;
    private final RootCauseAnalyzer rootCauseAnalyzer;
    private final List<RootCauseAnalyzer.RootCause> rootCauses;
    
    /**
     * Constructs a new ReportGenerator with the given analyzers and results.
     *
     * @param transactions the list of transactions analyzed
     * @param performanceAnalyzer the performance analyzer
     * @param memoryAnalyzer the memory analyzer
     * @param rootCauseAnalyzer the root cause analyzer
     * @param rootCauses the list of identified root causes
     */
    public ReportGenerator(
            List<Transaction> transactions,
            PerformanceAnalyzer performanceAnalyzer,
            MemoryAnalyzer memoryAnalyzer,
            RootCauseAnalyzer rootCauseAnalyzer,
            List<RootCauseAnalyzer.RootCause> rootCauses) {
        this.transactions = transactions;
        this.performanceAnalyzer = performanceAnalyzer;
        this.memoryAnalyzer = memoryAnalyzer;
        this.rootCauseAnalyzer = rootCauseAnalyzer;
        this.rootCauses = rootCauses;
        logger.info("Initialized ReportGenerator with {} transactions and {} root causes", 
                transactions.size(), rootCauses.size());
    }
    
    /**
     * Generates a report in the specified format.
     *
     * @param outputDirectory the directory to save the report to
     * @param format the report format (html, pdf, or docx)
     * @throws IOException if an I/O error occurs
     */
    public void generateReport(String outputDirectory, String format) throws IOException {
        logger.info("Generating {} report in directory: {}", format, outputDirectory);
        
        switch (format.toLowerCase()) {
            case "html":
                generateHtmlReport(outputDirectory);
                break;
            case "pdf":
                // PDF generation would typically use a library like iText
                // For simplicity, we'll generate HTML and note that it could be converted to PDF
                generateHtmlReport(outputDirectory);
                logger.info("PDF generation not directly supported. Generated HTML report instead.");
                break;
            case "docx":
                generateDocxReport(outputDirectory);
                break;
            default:
                logger.warn("Unsupported report format: {}. Generating HTML report instead.", format);
                generateHtmlReport(outputDirectory);
                break;
        }
        
        logger.info("Report generation completed successfully");
    }
    
    /**
     * Generates an HTML report.
     *
     * @param outputDirectory the directory to save the report to
     * @throws IOException if an I/O error occurs
     */
    private void generateHtmlReport(String outputDirectory) throws IOException {
        logger.info("Generating HTML report");
        
        // Create StringBuilder for HTML content
        StringBuilder html = new StringBuilder();
        
        // Add HTML header
        html.append("<!DOCTYPE html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("    <meta charset=\"UTF-8\">\n")
            .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("    <title>QServer Transaction Analysis Report</title>\n")
            .append("    <style>\n")
            .append("        body { font-family: Arial, sans-serif; margin: 20px; }\n")
            .append("        h1 { color: #2c3e50; }\n")
            .append("        h2 { color: #3498db; margin-top: 30px; }\n")
            .append("        h3 { color: #2980b9; }\n")
            .append("        table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }\n")
            .append("        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n")
            .append("        th { background-color: #f2f2f2; }\n")
            .append("        tr:nth-child(even) { background-color: #f9f9f9; }\n")
            .append("        .critical { color: #e74c3c; font-weight: bold; }\n")
            .append("        .high { color: #e67e22; font-weight: bold; }\n")
            .append("        .medium { color: #f39c12; }\n")
            .append("        .low { color: #27ae60; }\n")
            .append("        .chart { max-width: 100%; height: auto; margin: 20px 0; }\n")
            .append("        .recommendation { background-color: #eaf2f8; padding: 10px; border-left: 5px solid #3498db; margin-bottom: 10px; }\n")
            .append("    </style>\n")
            .append("</head>\n")
            .append("<body>\n");
        
        // Add report header
        html.append("    <h1>QServer Transaction Analysis Report</h1>\n")
            .append("    <p>Generated on: ").append(new Date()).append("</p>\n")
            .append("    <p>Total transactions analyzed: ").append(transactions.size()).append("</p>\n");
        
        // Add executive summary
        html.append("    <h2>Executive Summary</h2>\n")
            .append("    <p>This report presents the analysis of ").append(transactions.size())
            .append(" QServer transactions to identify performance bottlenecks, memory issues, and their root causes.</p>\n");
        
        // Add root causes section
        html.append("    <h2>Root Causes of Performance Issues</h2>\n");
        
        if (rootCauses.isEmpty()) {
            html.append("    <p>No significant performance issues were identified.</p>\n");
        } else {
            html.append("    <table>\n")
                .append("        <tr>\n")
                .append("            <th>Category</th>\n")
                .append("            <th>Description</th>\n")
                .append("            <th>Impact Level</th>\n")
                .append("            <th>Details</th>\n")
                .append("        </tr>\n");
            
            for (RootCauseAnalyzer.RootCause rootCause : rootCauses) {
                String impactLevelClass = "";
                switch (rootCause.getImpactLevel()) {
                    case "Critical":
                        impactLevelClass = "critical";
                        break;
                    case "High":
                        impactLevelClass = "high";
                        break;
                    case "Medium":
                        impactLevelClass = "medium";
                        break;
                    case "Low":
                        impactLevelClass = "low";
                        break;
                }
                
                html.append("        <tr>\n")
                    .append("            <td>").append(rootCause.getCategory()).append("</td>\n")
                    .append("            <td>").append(rootCause.getDescription()).append("</td>\n")
                    .append("            <td class=\"").append(impactLevelClass).append("\">")
                    .append(rootCause.getImpactLevel()).append("</td>\n")
                    .append("            <td>").append(rootCause.getDetails()).append("</td>\n")
                    .append("        </tr>\n");
            }
            
            html.append("    </table>\n");
        }
        
        // Add recommendations section
        html.append("    <h2>Recommendations</h2>\n");
        
        if (rootCauses.isEmpty()) {
            html.append("    <p>No specific recommendations are needed as no significant issues were identified.</p>\n");
        } else {
            for (RootCauseAnalyzer.RootCause rootCause : rootCauses) {
                html.append("    <h3>").append(rootCause.getCategory()).append(": ")
                    .append(rootCause.getDescription()).append("</h3>\n");
                
                List<String> recommendations = rootCause.getRecommendations();
                if (recommendations != null && !recommendations.isEmpty()) {
                    for (String recommendation : recommendations) {
                        html.append("    <div class=\"recommendation\">").append(recommendation).append("</div>\n");
                    }
                }
            }
        }
        
        // Add performance analysis section
        html.append("    <h2>Performance Analysis</h2>\n");
        
        // Add transaction length distribution chart
        html.append("    <h3>Transaction Length Distribution</h3>\n")
            .append("    <img src=\"charts/transaction_length_distribution.png\" alt=\"Transaction Length Distribution\" class=\"chart\">\n");
        
        // Add transaction waiting time distribution chart
        html.append("    <h3>Transaction Waiting Time Distribution</h3>\n")
            .append("    <img src=\"charts/transaction_waiting_time_distribution.png\" alt=\"Transaction Waiting Time Distribution\" class=\"chart\">\n");
        
        // Add transaction time components chart
        html.append("    <h3>Transaction Time Components</h3>\n")
            .append("    <img src=\"charts/transaction_time_components.png\" alt=\"Transaction Time Components\" class=\"chart\">\n");
        
        // Add transaction length by kind chart
        html.append("    <h3>Transaction Length by Kind</h3>\n")
            .append("    <img src=\"charts/transaction_length_by_kind.png\" alt=\"Transaction Length by Kind\" class=\"chart\">\n");
        
        // Add waiting time by kind chart
        html.append("    <h3>Waiting Time by Kind</h3>\n")
            .append("    <img src=\"charts/waiting_time_by_kind.png\" alt=\"Waiting Time by Kind\" class=\"chart\">\n");
        
        // Add transaction length variation chart
        html.append("    <h3>Transaction Length Variation</h3>\n")
            .append("    <img src=\"charts/transaction_length_variation.png\" alt=\"Transaction Length Variation\" class=\"chart\">\n");
        
        // Add memory analysis section
        html.append("    <h2>Memory Analysis</h2>\n");
        
        // Add memory usage over time chart
        html.append("    <h3>Memory Usage Over Time</h3>\n")
            .append("    <img src=\"charts/memory_usage_over_time.png\" alt=\"Memory Usage Over Time\" class=\"chart\">\n");
        
        // Add thread activity section
        html.append("    <h2>Thread Activity</h2>\n");
        
        // Add thread activity over time chart
        html.append("    <h3>Thread Activity Over Time</h3>\n")
            .append("    <img src=\"charts/thread_activity_over_time.png\" alt=\"Thread Activity Over Time\" class=\"chart\">\n");
        
        // Add transaction count by hour chart
        html.append("    <h3>Transaction Count by Hour</h3>\n")
            .append("    <img src=\"charts/transaction_count_by_hour.png\" alt=\"Transaction Count by Hour\" class=\"chart\">\n");
        
        // Add detailed statistics section
        html.append("    <h2>Detailed Statistics</h2>\n");
        
        // Add transaction statistics by kind
        Map<String, PerformanceAnalyzer.TransactionStats> kindStats = performanceAnalyzer.analyzeByTransactionKind();
        
        html.append("    <h3>Transaction Statistics by Kind</h3>\n")
            .append("    <table>\n")
            .append("        <tr>\n")
            .append("            <th>Transaction Kind</th>\n")
            .append("            <th>Count</th>\n")
            .append("            <th>Avg Length (ms)</th>\n")
            .append("            <th>Avg Waiting Time (ms)</th>\n")
            .append("            <th>Avg Processing Time (ms)</th>\n")
            .append("            <th>Avg DB Time (ms)</th>\n")
            .append("            <th>Dominant Component</th>\n")
            .append("        </tr>\n");
        
        // Sort by average length (descending)
        List<Map.Entry<String, PerformanceAnalyzer.TransactionStats>> sortedKindStats = 
                kindStats.entrySet().stream()
                        .sorted(Map.Entry.<String, PerformanceAnalyzer.TransactionStats>comparingByValue(
                                Comparator.comparingDouble(PerformanceAnalyzer.TransactionStats::getMeanLength).reversed()))
                        .collect(Collectors.toList());
        
        for (Map.Entry<String, PerformanceAnalyzer.TransactionStats> entry : sortedKindStats) {
            String kind = entry.getKey();
            PerformanceAnalyzer.TransactionStats stats = entry.getValue();
            
            html.append("        <tr>\n")
                .append("            <td>").append(kind).append("</td>\n")
                .append("            <td>").append(stats.getCount()).append("</td>\n")
                .append("            <td>").append(String.format("%.2f", stats.getMeanLength())).append("</td>\n")
                .append("            <td>").append(String.format("%.2f", stats.getMeanWaitingTime())).append("</td>\n")
                .append("            <td>").append(String.format("%.2f", stats.getMeanProcTime())).append("</td>\n")
                .append("            <td>").append(String.format("%.2f", stats.getMeanDbTime())).append("</td>\n")
                .append("            <td>").append(stats.getMostCommonDominantComponent()).append("</td>\n")
                .append("        </tr>\n");
        }
        
        html.append("    </table>\n");
        
        // Add memory statistics by kind
        Map<String, MemoryAnalyzer.MemoryStats> memoryKindStats = memoryAnalyzer.analyzeMemoryByTransactionKind();
        
        html.append("    <h3>Memory Statistics by Kind</h3>\n")
            .append("    <table>\n")
            .append("        <tr>\n")
            .append("            <th>Transaction Kind</th>\n")
            .append("            <th>Count</th>\n")
            .append("            <th>Avg Total Memory (KB)</th>\n")
            .append("            <th>Avg Proc Memory (KB)</th>\n")
            .append("            <th>Avg Func Memory (KB)</th>\n")
            .append("            <th>Avg DB Memory (KB)</th>\n")
            .append("        </tr>\n");
        
        // Sort by average total memory (descending)
        List<Map.Entry<String, MemoryAnalyzer.MemoryStats>> sortedMemoryKindStats = 
                memoryKindStats.entrySet().stream()
                        .sorted(Map.Entry.<String, MemoryAnalyzer.MemoryStats>comparingByValue(
                                Comparator.comparingDouble(MemoryAnalyzer.MemoryStats::getMeanTotalMemory).reversed()))
                        .collect(Collectors.toList());
        
        for (Map.Entry<String, MemoryAnalyzer.MemoryStats> entry : sortedMemoryKindStats) {
            String kind = entry.getKey();
            MemoryAnalyzer.MemoryStats stats = entry.getValue();
            
            html.append("        <tr>\n")
                .append("            <td>").append(kind).append("</td>\n")
                .append("            <td>").append(stats.getCount()).append("</td>\n")
                .append("            <td>").append(String.format("%.2f", stats.getMeanTotalMemory() / 1024)).append("</td>\n")
                .append("            <td>").append(String.format("%.2f", stats.getMeanProcMem() / 1024)).append("</td>\n")
                .append("            <td>").append(String.format("%.2f", stats.getMeanFuncMem() / 1024)).append("</td>\n")
                .append("            <td>").append(String.format("%.2f", stats.getMeanDbMem() / 1024)).append("</td>\n")
                .append("        </tr>\n");
        }
        
        html.append("    </table>\n");
        
        // Add HTML footer
        html.append("</body>\n")
            .append("</html>");
        
        // Write HTML to file
        File outputFile = new File(outputDirectory, "transaction_analysis_report.html");
        FileUtils.writeStringToFile(outputFile, html.toString(), "UTF-8");
        logger.info("HTML report saved to: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Generates a DOCX (Microsoft Word) report.
     *
     * @param outputDirectory the directory to save the report to
     * @throws IOException if an I/O error occurs
     */
    private void generateDocxReport(String outputDirectory) throws IOException {
        logger.info("Generating DOCX report");
        
        // Create new document
        XWPFDocument document = new XWPFDocument();
        
        // Add title
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText("QServer Transaction Analysis Report");
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        
        // Add generation date
        XWPFParagraph dateParagraph = document.createParagraph();
        XWPFRun dateRun = dateParagraph.createRun();
        dateRun.setText("Generated on: " + new Date());
        
        // Add transaction count
        XWPFParagraph countParagraph = document.createParagraph();
        XWPFRun countRun = countParagraph.createRun();
        countRun.setText("Total transactions analyzed: " + transactions.size());
        
        // Add executive summary
        addHeading(document, "Executive Summary", 1);
        
        XWPFParagraph summaryParagraph = document.createParagraph();
        XWPFRun summaryRun = summaryParagraph.createRun();
        summaryRun.setText("This report presents the analysis of " + transactions.size() + 
                " QServer transactions to identify performance bottlenecks, memory issues, and their root causes.");
        
        // Add root causes section
        addHeading(document, "Root Causes of Performance Issues", 1);
        
        if (rootCauses.isEmpty()) {
            XWPFParagraph noCausesParagraph = document.createParagraph();
            XWPFRun noCausesRun = noCausesParagraph.createRun();
            noCausesRun.setText("No significant performance issues were identified.");
        } else {
            // Create table for root causes
            XWPFTable rootCausesTable = document.createTable(rootCauses.size() + 1, 4);
            
            // Set table headers
            XWPFTableRow headerRow = rootCausesTable.getRow(0);
            headerRow.getCell(0).setText("Category");
            headerRow.getCell(1).setText("Description");
            headerRow.getCell(2).setText("Impact Level");
            headerRow.getCell(3).setText("Details");
            
            // Add root causes to table
            for (int i = 0; i < rootCauses.size(); i++) {
                RootCauseAnalyzer.RootCause rootCause = rootCauses.get(i);
                XWPFTableRow row = rootCausesTable.getRow(i + 1);
                
                row.getCell(0).setText(rootCause.getCategory());
                row.getCell(1).setText(rootCause.getDescription());
                row.getCell(2).setText(rootCause.getImpactLevel());
                row.getCell(3).setText(rootCause.getDetails());
            }
        }
        
        // Add recommendations section
        addHeading(document, "Recommendations", 1);
        
        if (rootCauses.isEmpty()) {
            XWPFParagraph noRecParagraph = document.createParagraph();
            XWPFRun noRecRun = noRecParagraph.createRun();
            noRecRun.setText("No specific recommendations are needed as no significant issues were identified.");
        } else {
            for (RootCauseAnalyzer.RootCause rootCause : rootCauses) {
                // Add recommendation heading
                addHeading(document, rootCause.getCategory() + ": " + rootCause.getDescription(), 2);
                
                // Add recommendations
                List<String> recommendations = rootCause.getRecommendations();
                if (recommendations != null && !recommendations.isEmpty()) {
                    for (String recommendation : recommendations) {
                        XWPFParagraph recParagraph = document.createParagraph();
                        recParagraph.setIndentationLeft(720); // 0.5 inch in twips
                        XWPFRun recRun = recParagraph.createRun();
                        recRun.setText("• " + recommendation);
                    }
                }
            }
        }
        
        // Add performance analysis section
        addHeading(document, "Performance Analysis", 1);
        
        // Add charts section
        addHeading(document, "Performance Charts", 2);
        
        // Note about charts
        XWPFParagraph chartsParagraph = document.createParagraph();
        XWPFRun chartsRun = chartsParagraph.createRun();
        chartsRun.setText("Performance charts are available in the 'charts' directory of the HTML report. " +
                "They include transaction length distribution, waiting time distribution, time components, " +
                "and transaction length by kind.");
        
        // Add detailed statistics section
        addHeading(document, "Detailed Statistics", 1);
        
        // Add transaction statistics by kind
        addHeading(document, "Transaction Statistics by Kind", 2);
        
        Map<String, PerformanceAnalyzer.TransactionStats> kindStats = performanceAnalyzer.analyzeByTransactionKind();
        
        // Sort by average length (descending)
        List<Map.Entry<String, PerformanceAnalyzer.TransactionStats>> sortedKindStats = 
                kindStats.entrySet().stream()
                        .sorted(Map.Entry.<String, PerformanceAnalyzer.TransactionStats>comparingByValue(
                                Comparator.comparingDouble(PerformanceAnalyzer.TransactionStats::getMeanLength).reversed()))
                        .collect(Collectors.toList());
        
        // Create table for transaction statistics
        XWPFTable statsTable = document.createTable(sortedKindStats.size() + 1, 7);
        
        // Set table headers
        XWPFTableRow statsHeaderRow = statsTable.getRow(0);
        statsHeaderRow.getCell(0).setText("Transaction Kind");
        statsHeaderRow.getCell(1).setText("Count");
        statsHeaderRow.getCell(2).setText("Avg Length (ms)");
        statsHeaderRow.getCell(3).setText("Avg Waiting Time (ms)");
        statsHeaderRow.getCell(4).setText("Avg Processing Time (ms)");
        statsHeaderRow.getCell(5).setText("Avg DB Time (ms)");
        statsHeaderRow.getCell(6).setText("Dominant Component");
        
        // Add statistics to table
        for (int i = 0; i < sortedKindStats.size(); i++) {
            Map.Entry<String, PerformanceAnalyzer.TransactionStats> entry = sortedKindStats.get(i);
            String kind = entry.getKey();
            PerformanceAnalyzer.TransactionStats stats = entry.getValue();
            
            XWPFTableRow row = statsTable.getRow(i + 1);
            row.getCell(0).setText(kind);
            row.getCell(1).setText(String.valueOf(stats.getCount()));
            row.getCell(2).setText(String.format("%.2f", stats.getMeanLength()));
            row.getCell(3).setText(String.format("%.2f", stats.getMeanWaitingTime()));
            row.getCell(4).setText(String.format("%.2f", stats.getMeanProcTime()));
            row.getCell(5).setText(String.format("%.2f", stats.getMeanDbTime()));
            row.getCell(6).setText(stats.getMostCommonDominantComponent());
        }
        
        // Add memory analysis section
        addHeading(document, "Memory Analysis", 1);
        
        // Add memory statistics by kind
        addHeading(document, "Memory Statistics by Kind", 2);
        
        Map<String, MemoryAnalyzer.MemoryStats> memoryKindStats = memoryAnalyzer.analyzeMemoryByTransactionKind();
        
        // Sort by average total memory (descending)
        List<Map.Entry<String, MemoryAnalyzer.MemoryStats>> sortedMemoryKindStats = 
                memoryKindStats.entrySet().stream()
                        .sorted(Map.Entry.<String, MemoryAnalyzer.MemoryStats>comparingByValue(
                                Comparator.comparingDouble(MemoryAnalyzer.MemoryStats::getMeanTotalMemory).reversed()))
                        .collect(Collectors.toList());
        
        // Create table for memory statistics
        XWPFTable memoryStatsTable = document.createTable(sortedMemoryKindStats.size() + 1, 6);
        
        // Set table headers
        XWPFTableRow memoryStatsHeaderRow = memoryStatsTable.getRow(0);
        memoryStatsHeaderRow.getCell(0).setText("Transaction Kind");
        memoryStatsHeaderRow.getCell(1).setText("Count");
        memoryStatsHeaderRow.getCell(2).setText("Avg Total Memory (KB)");
        memoryStatsHeaderRow.getCell(3).setText("Avg Proc Memory (KB)");
        memoryStatsHeaderRow.getCell(4).setText("Avg Func Memory (KB)");
        memoryStatsHeaderRow.getCell(5).setText("Avg DB Memory (KB)");
        
        // Add memory statistics to table
        for (int i = 0; i < sortedMemoryKindStats.size(); i++) {
            Map.Entry<String, MemoryAnalyzer.MemoryStats> entry = sortedMemoryKindStats.get(i);
            String kind = entry.getKey();
            MemoryAnalyzer.MemoryStats stats = entry.getValue();
            
            XWPFTableRow row = memoryStatsTable.getRow(i + 1);
            row.getCell(0).setText(kind);
            row.getCell(1).setText(String.valueOf(stats.getCount()));
            row.getCell(2).setText(String.format("%.2f", stats.getMeanTotalMemory() / 1024));
            row.getCell(3).setText(String.format("%.2f", stats.getMeanProcMem() / 1024));
            row.getCell(4).setText(String.format("%.2f", stats.getMeanFuncMem() / 1024));
            row.getCell(5).setText(String.format("%.2f", stats.getMeanDbMem() / 1024));
        }
        
        // Add conclusion
        addHeading(document, "Conclusion", 1);
        
        XWPFParagraph conclusionParagraph = document.createParagraph();
        XWPFRun conclusionRun = conclusionParagraph.createRun();
        conclusionRun.setText("This report has presented a comprehensive analysis of QServer transaction performance " +
                "and memory usage. By addressing the identified root causes and implementing the recommended " +
                "optimizations, the system's performance and stability can be significantly improved.");
        
        // Write document to file
        File outputFile = new File(outputDirectory, "QServer_Transaction_Analysis_Report.docx");
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            document.write(out);
        }
        
        logger.info("DOCX report saved to: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Adds a heading to the document.
     *
     * @param document the document to add the heading to
     * @param text the heading text
     * @param level the heading level (1-6)
     */
    private void addHeading(XWPFDocument document, String text, int level) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setStyle("Heading" + level);
        
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(true);
        
        // Set font size based on heading level
        switch (level) {
            case 1:
                run.setFontSize(14);
                break;
            case 2:
                run.setFontSize(13);
                break;
            default:
                run.setFontSize(12);
                break;
        }
    }
}
