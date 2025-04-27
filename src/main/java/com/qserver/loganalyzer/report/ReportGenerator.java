package com.qserver.loganalyzer.report;

import com.qserver.loganalyzer.model.Transaction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xwpf.usermodel.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Generates performance analysis reports in various formats.
 * This class provides methods to create Word documents, Excel spreadsheets,
 * and charts for visualizing transaction performance data.
 */
public class ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Generates a Word document report with performance analysis results.
     *
     * @param outputPath Path where the Word document will be saved
     * @param cpuIntensiveTransactions List of CPU-intensive transactions
     * @param cpuIntensiveKinds Map of transaction kinds to CPU usage
     * @param highWaitTimeTransactions List of transactions with high wait times
     * @param highWaitTimeKinds Map of transaction kinds to wait times
     * @param cpuIntensiveThreads Map of threads to CPU usage
     * @param resourceUtilization Map of resource types to utilization
     * @param transactionsCausingWaits List of transactions causing waits
     * @param activeThreadCount Number of active threads
     * @param maxConcurrentTransactions Maximum number of concurrent transactions
     * @param waitToProcessingRatio Overall wait-to-processing ratio
     * @throws IOException If an I/O error occurs
     */
    public void generateWordReport(String outputPath,
                                  List<Transaction> cpuIntensiveTransactions,
                                  Map<String, Double> cpuIntensiveKinds,
                                  List<Transaction> highWaitTimeTransactions,
                                  Map<String, Double> highWaitTimeKinds,
                                  Map<String, Double> cpuIntensiveThreads,
                                  Map<String, Double> resourceUtilization,
                                  List<Transaction> transactionsCausingWaits,
                                  int activeThreadCount,
                                  int maxConcurrentTransactions,
                                  double waitToProcessingRatio) throws IOException {
        logger.info("Generating Word report at: {}", outputPath);
        
        XWPFDocument document = new XWPFDocument();
        
        // Add title
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText("QServer Transaction Performance Analysis Report");
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        
        // Add executive summary
        XWPFParagraph summary = document.createParagraph();
        summary.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun summaryRun = summary.createRun();
        summaryRun.setText("Executive Summary");
        summaryRun.setBold(true);
        summaryRun.setFontSize(14);
        
        XWPFParagraph summaryText = document.createParagraph();
        summaryText.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun summaryTextRun = summaryText.createRun();
        summaryTextRun.setText("This report analyzes QServer transaction performance to identify bottlenecks and performance issues. " +
                "The analysis reveals key insights about CPU usage, waiting times, and resource utilization patterns.");
        
        // Add key metrics
        XWPFParagraph metricsTitle = document.createParagraph();
        metricsTitle.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun metricsTitleRun = metricsTitle.createRun();
        metricsTitleRun.setText("Key Performance Metrics");
        metricsTitleRun.setBold(true);
        metricsTitleRun.setFontSize(14);
        
        XWPFParagraph metrics = document.createParagraph();
        metrics.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun metricsRun = metrics.createRun();
        metricsRun.setText("• Active Thread Count: " + activeThreadCount);
        metricsRun.addBreak();
        metricsRun.setText("• Maximum Concurrent Transactions: " + maxConcurrentTransactions);
        metricsRun.addBreak();
        metricsRun.setText("• Wait-to-Processing Ratio: " + String.format("%.2f", waitToProcessingRatio));
        
        // Add CPU-intensive transactions section
        XWPFParagraph cpuTitle = document.createParagraph();
        cpuTitle.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun cpuTitleRun = cpuTitle.createRun();
        cpuTitleRun.setText("CPU-Intensive Transactions");
        cpuTitleRun.setBold(true);
        cpuTitleRun.setFontSize(14);
        
        // Create table for CPU-intensive transactions
        XWPFTable cpuTable = document.createTable(cpuIntensiveTransactions.size() + 1, 5);
        
        // Set table headers
        XWPFTableRow headerRow = cpuTable.getRow(0);
        headerRow.getCell(0).setText("Transaction ID");
        headerRow.getCell(1).setText("Kind");
        headerRow.getCell(2).setText("Thread");
        headerRow.getCell(3).setText("Processing Time (ms)");
        headerRow.getCell(4).setText("Wait Time (ms)");
        
        // Fill table with data
        for (int i = 0; i < cpuIntensiveTransactions.size(); i++) {
            Transaction transaction = cpuIntensiveTransactions.get(i);
            XWPFTableRow row = cpuTable.getRow(i + 1);
            
            row.getCell(0).setText(String.valueOf(transaction.getId()));
            row.getCell(1).setText(transaction.getKind());
            row.getCell(2).setText(transaction.getThread());
            row.getCell(3).setText(String.format("%.2f", transaction.getProcessingTime()));
            row.getCell(4).setText(String.format("%.2f", transaction.getWaitTime()));
        }
        
        // Add high wait time transactions section
        XWPFParagraph waitTitle = document.createParagraph();
        waitTitle.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun waitTitleRun = waitTitle.createRun();
        waitTitleRun.setText("Transactions with High Wait Times");
        waitTitleRun.setBold(true);
        waitTitleRun.setFontSize(14);
        
        // Create table for high wait time transactions
        XWPFTable waitTable = document.createTable(highWaitTimeTransactions.size() + 1, 5);
        
        // Set table headers
        headerRow = waitTable.getRow(0);
        headerRow.getCell(0).setText("Transaction ID");
        headerRow.getCell(1).setText("Kind");
        headerRow.getCell(2).setText("Thread");
        headerRow.getCell(3).setText("Wait Time (ms)");
        headerRow.getCell(4).setText("Processing Time (ms)");
        
        // Fill table with data
        for (int i = 0; i < highWaitTimeTransactions.size(); i++) {
            Transaction transaction = highWaitTimeTransactions.get(i);
            XWPFTableRow row = waitTable.getRow(i + 1);
            
            row.getCell(0).setText(String.valueOf(transaction.getId()));
            row.getCell(1).setText(transaction.getKind());
            row.getCell(2).setText(transaction.getThread());
            row.getCell(3).setText(String.format("%.2f", transaction.getWaitTime()));
            row.getCell(4).setText(String.format("%.2f", transaction.getProcessingTime()));
        }
        
        // Add transactions causing waits section
        XWPFParagraph causingTitle = document.createParagraph();
        causingTitle.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun causingTitleRun = causingTitle.createRun();
        causingTitleRun.setText("Transactions Causing Others to Wait");
        causingTitleRun.setBold(true);
        causingTitleRun.setFontSize(14);
        
        // Create table for transactions causing waits
        XWPFTable causingTable = document.createTable(transactionsCausingWaits.size() + 1, 5);
        
        // Set table headers
        headerRow = causingTable.getRow(0);
        headerRow.getCell(0).setText("Transaction ID");
        headerRow.getCell(1).setText("Kind");
        headerRow.getCell(2).setText("Thread");
        headerRow.getCell(3).setText("Processing Time (ms)");
        headerRow.getCell(4).setText("Details");
        
        // Fill table with data
        for (int i = 0; i < transactionsCausingWaits.size(); i++) {
            Transaction transaction = transactionsCausingWaits.get(i);
            XWPFTableRow row = causingTable.getRow(i + 1);
            
            row.getCell(0).setText(String.valueOf(transaction.getId()));
            row.getCell(1).setText(transaction.getKind());
            row.getCell(2).setText(transaction.getThread());
            row.getCell(3).setText(String.format("%.2f", transaction.getProcessingTime()));
            row.getCell(4).setText(transaction.getDetails());
        }
        
        // Add resource utilization section
        XWPFParagraph resourceTitle = document.createParagraph();
        resourceTitle.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun resourceTitleRun = resourceTitle.createRun();
        resourceTitleRun.setText("Resource Utilization");
        resourceTitleRun.setBold(true);
        resourceTitleRun.setFontSize(14);
        
        // Create table for resource utilization
        XWPFTable resourceTable = document.createTable(resourceUtilization.size() + 1, 2);
        
        // Set table headers
        headerRow = resourceTable.getRow(0);
        headerRow.getCell(0).setText("Resource Type");
        headerRow.getCell(1).setText("Total Time (ms)");
        
        // Fill table with data
        int rowIndex = 1;
        for (Map.Entry<String, Double> entry : resourceUtilization.entrySet()) {
            XWPFTableRow row = resourceTable.getRow(rowIndex++);
            row.getCell(0).setText(entry.getKey());
            row.getCell(1).setText(String.format("%.2f", entry.getValue()));
        }
        
        // Add recommendations section
        XWPFParagraph recommendationsTitle = document.createParagraph();
        recommendationsTitle.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun recommendationsTitleRun = recommendationsTitle.createRun();
        recommendationsTitleRun.setText("Recommendations");
        recommendationsTitleRun.setBold(true);
        recommendationsTitleRun.setFontSize(14);
        
        XWPFParagraph recommendations = document.createParagraph();
        recommendations.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun recommendationsRun = recommendations.createRun();
        
        // Generate recommendations based on analysis
        if (waitToProcessingRatio > 10) {
            recommendationsRun.setText("1. Increase thread pool size to reduce waiting times.");
            recommendationsRun.addBreak();
        }
        
        recommendationsRun.setText("2. Optimize CPU-intensive transactions, particularly of type: " + 
                                  String.join(", ", cpuIntensiveKinds.keySet().stream().limit(3).toArray(String[]::new)));
        recommendationsRun.addBreak();
        
        if (maxConcurrentTransactions > activeThreadCount * 0.8) {
            recommendationsRun.setText("3. Consider increasing system resources as thread pool is near saturation.");
            recommendationsRun.addBreak();
        }
        
        recommendationsRun.setText("4. Review transaction scheduling to reduce contention on threads: " + 
                                  String.join(", ", cpuIntensiveThreads.keySet().stream().limit(3).toArray(String[]::new)));
        
        // Save the document
        try (FileOutputStream out = new FileOutputStream(outputPath)) {
            document.write(out);
        }
        
        logger.info("Word report generated successfully at: {}", outputPath);
    }
    
    /**
     * Generates an Excel spreadsheet with performance analysis results.
     *
     * @param outputPath Path where the Excel file will be saved
     * @param cpuIntensiveTransactions List of CPU-intensive transactions
     * @param cpuIntensiveKinds Map of transaction kinds to CPU usage
     * @param highWaitTimeTransactions List of transactions with high wait times
     * @param highWaitTimeKinds Map of transaction kinds to wait times
     * @param cpuIntensiveThreads Map of threads to CPU usage
     * @param resourceUtilization Map of resource types to utilization
     * @param transactionsCausingWaits List of transactions causing waits
     * @throws IOException If an I/O error occurs
     */
    public void generateExcelReport(String outputPath,
                                   List<Transaction> cpuIntensiveTransactions,
                                   Map<String, Double> cpuIntensiveKinds,
                                   List<Transaction> highWaitTimeTransactions,
                                   Map<String, Double> highWaitTimeKinds,
                                   Map<String, Double> cpuIntensiveThreads,
                                   Map<String, Double> resourceUtilization,
                                   List<Transaction> transactionsCausingWaits) throws IOException {
        logger.info("Generating Excel report at: {}", outputPath);
        
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        // Create styles
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        // Create CPU-intensive transactions sheet
        XSSFSheet cpuSheet = workbook.createSheet("CPU Intensive Transactions");
        
        // Create header row
        Row cpuHeaderRow = cpuSheet.createRow(0);
        Cell cell = cpuHeaderRow.createCell(0);
        cell.setCellValue("Transaction ID");
        cell.setCellStyle(headerStyle);
        
        cell = cpuHeaderRow.createCell(1);
        cell.setCellValue("Kind");
        cell.setCellStyle(headerStyle);
        
        cell = cpuHeaderRow.createCell(2);
        cell.setCellValue("Thread");
        cell.setCellStyle(headerStyle);
        
        cell = cpuHeaderRow.createCell(3);
        cell.setCellValue("Start Time");
        cell.setCellStyle(headerStyle);
        
        cell = cpuHeaderRow.createCell(4);
        cell.setCellValue("End Time");
        cell.setCellStyle(headerStyle);
        
        cell = cpuHeaderRow.createCell(5);
        cell.setCellValue("Processing Time (ms)");
        cell.setCellStyle(headerStyle);
        
        cell = cpuHeaderRow.createCell(6);
        cell.setCellValue("Wait Time (ms)");
        cell.setCellStyle(headerStyle);
        
        // Fill data rows
        for (int i = 0; i < cpuIntensiveTransactions.size(); i++) {
            Transaction transaction = cpuIntensiveTransactions.get(i);
            Row row = cpuSheet.createRow(i + 1);
            
            row.createCell(0).setCellValue(transaction.getId());
            row.createCell(1).setCellValue(transaction.getKind());
            row.createCell(2).setCellValue(transaction.getThread());
            
            if (transaction.getStartTime() != null) {
                row.createCell(3).setCellValue(transaction.getStartTime().format(DATE_TIME_FORMATTER));
            }
            
            if (transaction.getEndTime() != null) {
                row.createCell(4).setCellValue(transaction.getEndTime().format(DATE_TIME_FORMATTER));
            }
            
            row.createCell(5).setCellValue(transaction.getProcessingTime());
            row.createCell(6).setCellValue(transaction.getWaitTime());
        }
        
        // Auto-size columns
        for (int i = 0; i < 7; i++) {
            cpuSheet.autoSizeColumn(i);
        }
        
        // Create high wait time transactions sheet
        XSSFSheet waitSheet = workbook.createSheet("High Wait Time Transactions");
        
        // Create header row
        Row waitHeaderRow = waitSheet.createRow(0);
        cell = waitHeaderRow.createCell(0);
        cell.setCellValue("Transaction ID");
        cell.setCellStyle(headerStyle);
        
        cell = waitHeaderRow.createCell(1);
        cell.setCellValue("Kind");
        cell.setCellStyle(headerStyle);
        
        cell = waitHeaderRow.createCell(2);
        cell.setCellValue("Thread");
        cell.setCellStyle(headerStyle);
        
        cell = waitHeaderRow.createCell(3);
        cell.setCellValue("Start Time");
        cell.setCellStyle(headerStyle);
        
        cell = waitHeaderRow.createCell(4);
        cell.setCellValue("End Time");
        cell.setCellStyle(headerStyle);
        
        cell = waitHeaderRow.createCell(5);
        cell.setCellValue("Wait Time (ms)");
        cell.setCellStyle(headerStyle);
        
        cell = waitHeaderRow.createCell(6);
        cell.setCellValue("Processing Time (ms)");
        cell.setCellStyle(headerStyle);
        
        // Fill data rows
        for (int i = 0; i < highWaitTimeTransactions.size(); i++) {
            Transaction transaction = highWaitTimeTransactions.get(i);
            Row row = waitSheet.createRow(i + 1);
            
            row.createCell(0).setCellValue(transaction.getId());
            row.createCell(1).setCellValue(transaction.getKind());
            row.createCell(2).setCellValue(transaction.getThread());
            
            if (transaction.getStartTime() != null) {
                row.createCell(3).setCellValue(transaction.getStartTime().format(DATE_TIME_FORMATTER));
            }
            
            if (transaction.getEndTime() != null) {
                row.createCell(4).setCellValue(transaction.getEndTime().format(DATE_TIME_FORMATTER));
            }
            
            row.createCell(5).setCellValue(transaction.getWaitTime());
            row.createCell(6).setCellValue(transaction.getProcessingTime());
        }
        
        // Auto-size columns
        for (int i = 0; i < 7; i++) {
            waitSheet.autoSizeColumn(i);
        }
        
        // Create transactions causing waits sheet
        XSSFSheet causingSheet = workbook.createSheet("Transactions Causing Waits");
        
        // Create header row
        Row causingHeaderRow = causingSheet.createRow(0);
        cell = causingHeaderRow.createCell(0);
        cell.setCellValue("Transaction ID");
        cell.setCellStyle(headerStyle);
        
        cell = causingHeaderRow.createCell(1);
        cell.setCellValue("Kind");
        cell.setCellStyle(headerStyle);
        
        cell = causingHeaderRow.createCell(2);
        cell.setCellValue("Thread");
        cell.setCellStyle(headerStyle);
        
        cell = causingHeaderRow.createCell(3);
        cell.setCellValue("Start Time");
        cell.setCellStyle(headerStyle);
        
        cell = causingHeaderRow.createCell(4);
        cell.setCellValue("End Time");
        cell.setCellStyle(headerStyle);
        
        cell = causingHeaderRow.createCell(5);
        cell.setCellValue("Processing Time (ms)");
        cell.setCellStyle(headerStyle);
        
        cell = causingHeaderRow.createCell(6);
        cell.setCellValue("Details");
        cell.setCellStyle(headerStyle);
        
        // Fill data rows
        for (int i = 0; i < transactionsCausingWaits.size(); i++) {
            Transaction transaction = transactionsCausingWaits.get(i);
            Row row = causingSheet.createRow(i + 1);
            
            row.createCell(0).setCellValue(transaction.getId());
            row.createCell(1).setCellValue(transaction.getKind());
            row.createCell(2).setCellValue(transaction.getThread());
            
            if (transaction.getStartTime() != null) {
                row.createCell(3).setCellValue(transaction.getStartTime().format(DATE_TIME_FORMATTER));
            }
            
            if (transaction.getEndTime() != null) {
                row.createCell(4).setCellValue(transaction.getEndTime().format(DATE_TIME_FORMATTER));
            }
            
            row.createCell(5).setCellValue(transaction.getProcessingTime());
            row.createCell(6).setCellValue(transaction.getDetails());
        }
        
        // Auto-size columns
        for (int i = 0; i < 7; i++) {
            causingSheet.autoSizeColumn(i);
        }
        
        // Create summary sheet with charts
        XSSFSheet summarySheet = workbook.createSheet("Summary");
        
        // Add title
        Row titleRow = summarySheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("QServer Transaction Performance Analysis");
        XSSFCellStyle titleStyle = workbook.createCellStyle();
        XSSFFont titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        
        // Add CPU-intensive kinds data
        Row cpuKindsHeaderRow = summarySheet.createRow(2);
        cell = cpuKindsHeaderRow.createCell(0);
        cell.setCellValue("Transaction Kind");
        cell.setCellStyle(headerStyle);
        
        cell = cpuKindsHeaderRow.createCell(1);
        cell.setCellValue("CPU Time (ms)");
        cell.setCellStyle(headerStyle);
        
        int rowIndex = 3;
        for (Map.Entry<String, Double> entry : cpuIntensiveKinds.entrySet()) {
            Row row = summarySheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
        
        // Add high wait time kinds data
        Row waitKindsHeaderRow = summarySheet.createRow(rowIndex + 1);
        cell = waitKindsHeaderRow.createCell(0);
        cell.setCellValue("Transaction Kind");
        cell.setCellStyle(headerStyle);
        
        cell = waitKindsHeaderRow.createCell(1);
        cell.setCellValue("Wait Time (ms)");
        cell.setCellStyle(headerStyle);
        
        rowIndex += 2;
        for (Map.Entry<String, Double> entry : highWaitTimeKinds.entrySet()) {
            Row row = summarySheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
        
        // Add resource utilization data
        Row resourceHeaderRow = summarySheet.createRow(rowIndex + 1);
        cell = resourceHeaderRow.createCell(0);
        cell.setCellValue("Resource Type");
        cell.setCellStyle(headerStyle);
        
        cell = resourceHeaderRow.createCell(1);
        cell.setCellValue("Total Time (ms)");
        cell.setCellStyle(headerStyle);
        
        rowIndex += 2;
        for (Map.Entry<String, Double> entry : resourceUtilization.entrySet()) {
            Row row = summarySheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
        
        // Auto-size columns
        for (int i = 0; i < 2; i++) {
            summarySheet.autoSizeColumn(i);
        }
        
        // Save the workbook
        try (FileOutputStream out = new FileOutputStream(outputPath)) {
            workbook.write(out);
        }
        
        logger.info("Excel report generated successfully at: {}", outputPath);
    }
    
    /**
     * Generates charts for visualizing performance data.
     *
     * @param outputDir Directory where chart images will be saved
     * @param cpuIntensiveKinds Map of transaction kinds to CPU usage
     * @param highWaitTimeKinds Map of transaction kinds to wait times
     * @param resourceUtilization Map of resource types to utilization
     * @throws IOException If an I/O error occurs
     */
    public void generateCharts(String outputDir,
                              Map<String, Double> cpuIntensiveKinds,
                              Map<String, Double> highWaitTimeKinds,
                              Map<String, Double> resourceUtilization) throws IOException {
        logger.info("Generating charts in directory: {}", outputDir);
        
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // Generate CPU usage by transaction kind chart
        DefaultCategoryDataset cpuDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : cpuIntensiveKinds.entrySet()) {
            cpuDataset.addValue(entry.getValue(), "CPU Time", entry.getKey());
        }
        
        JFreeChart cpuChart = ChartFactory.createBarChart(
                "CPU Usage by Transaction Kind",
                "Transaction Kind",
                "CPU Time (ms)",
                cpuDataset
        );
        
        File cpuChartFile = new File(outputDir, "cpu_usage_by_kind.png");
        ChartUtils.saveChartAsPNG(cpuChartFile, cpuChart, 800, 600);
        
        // Generate wait time by transaction kind chart
        DefaultCategoryDataset waitDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : highWaitTimeKinds.entrySet()) {
            waitDataset.addValue(entry.getValue(), "Wait Time", entry.getKey());
        }
        
        JFreeChart waitChart = ChartFactory.createBarChart(
                "Wait Time by Transaction Kind",
                "Transaction Kind",
                "Wait Time (ms)",
                waitDataset
        );
        
        File waitChartFile = new File(outputDir, "wait_time_by_kind.png");
        ChartUtils.saveChartAsPNG(waitChartFile, waitChart, 800, 600);
        
        // Generate resource utilization pie chart
        DefaultPieDataset<String> resourceDataset = new DefaultPieDataset<>();
        for (Map.Entry<String, Double> entry : resourceUtilization.entrySet()) {
            resourceDataset.setValue(entry.getKey(), entry.getValue());
        }
        
        JFreeChart resourceChart = ChartFactory.createPieChart(
                "Resource Utilization",
                resourceDataset,
                true,
                true,
                false
        );
        
        PiePlot plot = (PiePlot) resourceChart.getPlot();
        plot.setLabelGenerator(null);  // Remove labels from pie slices
        
        File resourceChartFile = new File(outputDir, "resource_utilization.png");
        ChartUtils.saveChartAsPNG(resourceChartFile, resourceChart, 800, 600);
        
        logger.info("Charts generated successfully in directory: {}", outputDir);
    }
}
