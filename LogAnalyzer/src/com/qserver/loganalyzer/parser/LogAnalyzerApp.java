package com.qserver.loganalyzer;

import com.qserver.loganalyzer.analyzer.MemoryAnalyzer;
import com.qserver.loganalyzer.analyzer.PerformanceAnalyzer;
import com.qserver.loganalyzer.analyzer.RootCauseAnalyzer;
import com.qserver.loganalyzer.model.Transaction;
import com.qserver.loganalyzer.parser.TransactionLogParser;
import com.qserver.loganalyzer.report.ReportGenerator;
import com.qserver.loganalyzer.visualization.ChartGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main application class for the QServer Log Analyzer.
 * This class provides the entry point for the application and coordinates the analysis process.
 */
public class LogAnalyzerApp {
    private static final Logger logger = LoggerFactory.getLogger(LogAnalyzerApp.class);
    
    /**
     * Main method - entry point for the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        logger.info("Starting QServer Log Analyzer");
        
        try {
            // Parse command line arguments
            AnalysisOptions options = parseCommandLineArgs(args);
            
            // Find log files
            List<File> transactionLogFiles = findLogFiles(options.getLogDirectory(), "QServerTrans_", ".csv");
            
            if (transactionLogFiles.isEmpty()) {
                logger.error("No transaction log files found in directory: {}", options.getLogDirectory());
                System.exit(1);
            }
            
            logger.info("Found {} transaction log files", transactionLogFiles.size());
            
            // Parse transaction logs
            TransactionLogParser parser = new TransactionLogParser();
            List<Transaction> transactions = parser.parseFiles(transactionLogFiles);
            
            if (transactions.isEmpty()) {
                logger.error("No transactions parsed from log files");
                System.exit(1);
            }
            
            logger.info("Parsed {} transactions from log files", transactions.size());
            
            // Perform performance analysis
            PerformanceAnalyzer performanceAnalyzer = new PerformanceAnalyzer(transactions);
            
            // Perform memory analysis
            MemoryAnalyzer memoryAnalyzer = new MemoryAnalyzer(transactions);
            
            // Perform root cause analysis
            RootCauseAnalyzer rootCauseAnalyzer = new RootCauseAnalyzer(transactions);
            List<RootCauseAnalyzer.RootCause> rootCauses = rootCauseAnalyzer.identifyRootCauses();
            
            // Generate charts
            ChartGenerator chartGenerator = new ChartGenerator(transactions);
            chartGenerator.generateCharts(options.getOutputDirectory());
            
            // Generate report
            ReportGenerator reportGenerator = new ReportGenerator(
                    transactions, 
                    performanceAnalyzer, 
                    memoryAnalyzer, 
                    rootCauseAnalyzer,
                    rootCauses);
            
            reportGenerator.generateReport(options.getOutputDirectory(), options.getReportFormat());
            
            logger.info("Analysis completed successfully. Report generated at: {}", 
                    options.getOutputDirectory());
            
        } catch (Exception e) {
            logger.error("Error during analysis: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
    
    /**
     * Parses command line arguments into analysis options.
     *
     * @param args command line arguments
     * @return the parsed analysis options
     */
    private static AnalysisOptions parseCommandLineArgs(String[] args) {
        AnalysisOptions options = new AnalysisOptions();
        
        // Set default values
        options.setLogDirectory(System.getProperty("user.dir"));
        options.setOutputDirectory(System.getProperty("user.dir") + File.separator + "output");
        options.setReportFormat("html");
        
        // Parse arguments
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            if ("-d".equals(arg) || "--directory".equals(arg)) {
                if (i + 1 < args.length) {
                    options.setLogDirectory(args[++i]);
                }
            } else if ("-o".equals(arg) || "--output".equals(arg)) {
                if (i + 1 < args.length) {
                    options.setOutputDirectory(args[++i]);
                }
            } else if ("-f".equals(arg) || "--format".equals(arg)) {
                if (i + 1 < args.length) {
                    options.setReportFormat(args[++i]);
                }
            } else if ("-h".equals(arg) || "--help".equals(arg)) {
                printHelp();
                System.exit(0);
            }
        }
        
        // Create output directory if it doesn't exist
        File outputDir = new File(options.getOutputDirectory());
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                logger.error("Failed to create output directory: {}", options.getOutputDirectory());
                System.exit(1);
            }
        }
        
        return options;
    }
    
    /**
     * Finds log files in the specified directory with the given prefix and suffix.
     *
     * @param directory the directory to search in
     * @param prefix the file name prefix to match
     * @param suffix the file name suffix to match
     * @return a list of matching log files
     */
    private static List<File> findLogFiles(String directory, String prefix, String suffix) {
        List<File> logFiles = new ArrayList<>();
        
        try {
            Path dirPath = Paths.get(directory);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                logger.error("Directory does not exist or is not a directory: {}", directory);
                return logFiles;
            }
            
            logFiles = Files.walk(dirPath)
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(file -> file.getName().startsWith(prefix) && file.getName().endsWith(suffix))
                    .collect(Collectors.toList());
            
        } catch (IOException e) {
            logger.error("Error finding log files: {}", e.getMessage(), e);
        }
        
        return logFiles;
    }
    
    /**
     * Prints help information for the application.
     */
    private static void printHelp() {
        System.out.println("QServer Log Analyzer");
        System.out.println("Usage: java -jar log-analyzer.jar [options]");
        System.out.println("Options:");
        System.out.println("  -d, --directory <dir>   Directory containing log files (default: current directory)");
        System.out.println("  -o, --output <dir>      Output directory for reports (default: ./output)");
        System.out.println("  -f, --format <format>   Report format: html, pdf, or docx (default: html)");
        System.out.println("  -h, --help              Show this help message");
    }
    
    /**
     * Inner class to hold analysis options.
     */
    private static class AnalysisOptions {
        private String logDirectory;
        private String outputDirectory;
        private String reportFormat;
        
        public String getLogDirectory() {
            return logDirectory;
        }
        
        public void setLogDirectory(String logDirectory) {
            this.logDirectory = logDirectory;
        }
        
        public String getOutputDirectory() {
            return outputDirectory;
        }
        
        public void setOutputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
        }
        
        public String getReportFormat() {
            return reportFormat;
        }
        
        public void setReportFormat(String reportFormat) {
            this.reportFormat = reportFormat;
        }
    }
}
