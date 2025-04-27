package com.qserver.loganalyzer;

import com.qserver.loganalyzer.analyzer.PerformanceAnalyzer;
import com.qserver.loganalyzer.model.Transaction;
import com.qserver.loganalyzer.parser.TransactionLogParser;
import com.qserver.loganalyzer.report.ReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main application class for the QServer Transaction Log Analyzer.
 * This class provides the entry point and orchestrates the analysis process.
 */
public class TransactionLogAnalyzerApp {
    private static final Logger logger = LoggerFactory.getLogger(TransactionLogAnalyzerApp.class);
    
    // Default values for analysis parameters
    private static final int DEFAULT_TOP_TRANSACTIONS = 10;
    private static final int DEFAULT_TOP_KINDS = 5;
    private static final int DEFAULT_TOP_THREADS = 5;
    
    /**
     * Main method - entry point for the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        logger.info("Starting QServer Transaction Log Analyzer");
        
        if (args.length < 1) {
            printUsage();
            return;
        }
        
        try {
            // Parse command line arguments
            String logDir = args[0];
            String outputDir = args.length > 1 ? args[1] : "output";
            
            // Create output directory if it doesn't exist
            Path outputPath = Paths.get(outputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
            
            // Run the analysis
            runAnalysis(logDir, outputDir);
            
            logger.info("Analysis completed successfully. Results saved to: {}", outputDir);
        } catch (Exception e) {
            logger.error("Error during analysis: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Prints usage information.
     */
    private static void printUsage() {
        System.out.println("Usage: java -jar transaction-log-analyzer.jar <log_directory> [output_directory]");
        System.out.println("  <log_directory>   : Directory containing QServer transaction log files (CSV format)");
        System.out.println("  [output_directory]: Optional directory for output reports (default: 'output')");
    }
    
    /**
     * Runs the performance analysis on transaction logs.
     *
     * @param logDir Directory containing log files
     * @param outputDir Directory for output reports
     * @throws IOException If an I/O error occurs
     */
    private static void runAnalysis(String logDir, String outputDir) throws IOException {
        logger.info("Analyzing transaction logs in directory: {}", logDir);
        
        // Find transaction log files
        List<String> transactionLogFiles = findTransactionLogFiles(logDir);
        if (transactionLogFiles.isEmpty()) {
            logger.warn("No transaction log files found in directory: {}", logDir);
            System.out.println("No transaction log files found in directory: " + logDir);
            return;
        }
        
        logger.info("Found {} transaction log files", transactionLogFiles.size());
        
        // Parse transaction logs
        List<Transaction> transactions = parseTransactionLogs(transactionLogFiles);
        if (transactions.isEmpty()) {
            logger.warn("No transactions parsed from log files");
            System.out.println("No transactions parsed from log files");
            return;
        }
        
        logger.info("Parsed {} transactions from log files", transactions.size());
        
        // Analyze transactions
        PerformanceAnalyzer analyzer = new PerformanceAnalyzer(transactions);
        
        // Get analysis results
        List<Transaction> cpuIntensiveTransactions = analyzer.identifyCpuIntensiveTransactions(DEFAULT_TOP_TRANSACTIONS);
        Map<String, Double> cpuIntensiveKinds = analyzer.identifyCpuIntensiveTransactionKinds(DEFAULT_TOP_KINDS);
        List<Transaction> highWaitTimeTransactions = analyzer.identifyHighWaitTimeTransactions(DEFAULT_TOP_TRANSACTIONS);
        Map<String, Double> highWaitTimeKinds = analyzer.identifyHighWaitTimeTransactionKinds(DEFAULT_TOP_KINDS);
        Map<String, Double> cpuIntensiveThreads = analyzer.identifyCpuIntensiveThreads(DEFAULT_TOP_THREADS);
        Map<String, Double> resourceUtilization = analyzer.analyzeResourceUtilization();
        List<Transaction> transactionsCausingWaits = analyzer.identifyTransactionsCausingWaits(DEFAULT_TOP_TRANSACTIONS);
        int activeThreadCount = analyzer.calculateActiveThreadCount();
        int maxConcurrentTransactions = analyzer.calculateMaxConcurrentTransactions();
        double waitToProcessingRatio = analyzer.calculateOverallWaitToProcessingRatio();
        
        // Generate reports
        ReportGenerator reportGenerator = new ReportGenerator();
        
        // Generate Word report
        String wordReportPath = outputDir + File.separator + "QServer_Performance_Analysis_Report.docx";
        reportGenerator.generateWordReport(
                wordReportPath,
                cpuIntensiveTransactions,
                cpuIntensiveKinds,
                highWaitTimeTransactions,
                highWaitTimeKinds,
                cpuIntensiveThreads,
                resourceUtilization,
                transactionsCausingWaits,
                activeThreadCount,
                maxConcurrentTransactions,
                waitToProcessingRatio
        );
        
        // Generate Excel report
        String excelReportPath = outputDir + File.separator + "QServer_Performance_Analysis_Data.xlsx";
        reportGenerator.generateExcelReport(
                excelReportPath,
                cpuIntensiveTransactions,
                cpuIntensiveKinds,
                highWaitTimeTransactions,
                highWaitTimeKinds,
                cpuIntensiveThreads,
                resourceUtilization,
                transactionsCausingWaits
        );
        
        // Generate charts
        String chartsDir = outputDir + File.separator + "charts";
        reportGenerator.generateCharts(
                chartsDir,
                cpuIntensiveKinds,
                highWaitTimeKinds,
                resourceUtilization
        );
        
        // Print summary to console
        printSummary(
                cpuIntensiveKinds,
                highWaitTimeKinds,
                activeThreadCount,
                maxConcurrentTransactions,
                waitToProcessingRatio
        );
    }
    
    /**
     * Finds transaction log files in the specified directory.
     *
     * @param logDir Directory to search for log files
     * @return List of transaction log file paths
     */
    private static List<String> findTransactionLogFiles(String logDir) {
        List<String> transactionLogFiles = new ArrayList<>();
        
        File dir = new File(logDir);
        if (!dir.exists() || !dir.isDirectory()) {
            logger.warn("Log directory does not exist or is not a directory: {}", logDir);
            return transactionLogFiles;
        }
        
        File[] files = dir.listFiles((d, name) -> 
                name.toLowerCase().contains("trans") && name.toLowerCase().endsWith(".csv"));
        
        if (files != null) {
            for (File file : files) {
                transactionLogFiles.add(file.getAbsolutePath());
            }
        }
        
        return transactionLogFiles;
    }
    
    /**
     * Parses transaction logs from the specified files.
     *
     * @param logFiles List of log file paths
     * @return List of parsed transactions
     */
    private static List<Transaction> parseTransactionLogs(List<String> logFiles) {
        List<Transaction> allTransactions = new ArrayList<>();
        TransactionLogParser parser = new TransactionLogParser();
        
        for (String logFile : logFiles) {
            try {
                List<Transaction> transactions = parser.parseTransactionLog(logFile);
                allTransactions.addAll(transactions);
                logger.info("Parsed {} transactions from file: {}", transactions.size(), logFile);
            } catch (IOException e) {
                logger.error("Error parsing log file {}: {}", logFile, e.getMessage(), e);
            }
        }
        
        return allTransactions;
    }
    
    /**
     * Prints a summary of the analysis results to the console.
     *
     * @param cpuIntensiveKinds Map of transaction kinds to CPU usage
     * @param highWaitTimeKinds Map of transaction kinds to wait times
     * @param activeThreadCount Number of active threads
     * @param maxConcurrentTransactions Maximum number of concurrent transactions
     * @param waitToProcessingRatio Overall wait-to-processing ratio
     */
    private static void printSummary(
            Map<String, Double> cpuIntensiveKinds,
            Map<String, Double> highWaitTimeKinds,
            int activeThreadCount,
            int maxConcurrentTransactions,
            double waitToProcessingRatio) {
        
        System.out.println("\n=== QServer Transaction Performance Analysis Summary ===");
        System.out.println("Active Thread Count: " + activeThreadCount);
        System.out.println("Maximum Concurrent Transactions: " + maxConcurrentTransactions);
        System.out.println("Wait-to-Processing Ratio: " + String.format("%.2f", waitToProcessingRatio));
        
        System.out.println("\nTop CPU-Intensive Transaction Kinds:");
        int count = 1;
        for (Map.Entry<String, Double> entry : cpuIntensiveKinds.entrySet()) {
            System.out.println(count + ". " + entry.getKey() + ": " + String.format("%.2f", entry.getValue()) + " ms");
            if (count++ >= 3) break;
        }
        
        System.out.println("\nTop Transaction Kinds with High Wait Times:");
        count = 1;
        for (Map.Entry<String, Double> entry : highWaitTimeKinds.entrySet()) {
            System.out.println(count + ". " + entry.getKey() + ": " + String.format("%.2f", entry.getValue()) + " ms");
            if (count++ >= 3) break;
        }
        
        System.out.println("\nReports have been generated in the output directory.");
        System.out.println("=======================================================");
    }
}
