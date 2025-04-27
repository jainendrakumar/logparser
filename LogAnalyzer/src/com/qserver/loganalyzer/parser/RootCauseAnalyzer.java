package com.qserver.loganalyzer.analyzer;

import com.qserver.loganalyzer.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Performs root cause analysis on transaction data to identify the underlying causes of performance issues.
 * This class provides methods for analyzing various aspects of transaction performance and identifying root causes.
 */
public class RootCauseAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(RootCauseAnalyzer.class);
    
    private final List<Transaction> transactions;
    private final PerformanceAnalyzer performanceAnalyzer;
    private final MemoryAnalyzer memoryAnalyzer;
    
    /**
     * Constructs a new RootCauseAnalyzer with the given transactions.
     *
     * @param transactions the list of transactions to analyze
     */
    public RootCauseAnalyzer(List<Transaction> transactions) {
        this.transactions = new ArrayList<>(transactions);
        this.performanceAnalyzer = new PerformanceAnalyzer(transactions);
        this.memoryAnalyzer = new MemoryAnalyzer(transactions);
        logger.info("Initialized RootCauseAnalyzer with {} transactions", transactions.size());
    }
    
    /**
     * Identifies the root causes of performance issues.
     *
     * @return a list of root causes with their details
     */
    public List<RootCause> identifyRootCauses() {
        List<RootCause> rootCauses = new ArrayList<>();
        
        // Analyze waiting time issues
        analyzeWaitingTimeIssues(rootCauses);
        
        // Analyze processing time issues
        analyzeProcessingTimeIssues(rootCauses);
        
        // Analyze database time issues
        analyzeDatabaseTimeIssues(rootCauses);
        
        // Analyze memory issues
        analyzeMemoryIssues(rootCauses);
        
        // Analyze thread pool issues
        analyzeThreadPoolIssues(rootCauses);
        
        // Sort by impact score (descending)
        rootCauses.sort(Comparator.comparing(RootCause::getImpactScore).reversed());
        
        logger.info("Identified {} root causes of performance issues", rootCauses.size());
        return rootCauses;
    }
    
    /**
     * Analyzes waiting time issues and adds root causes to the list.
     *
     * @param rootCauses the list of root causes to add to
     */
    private void analyzeWaitingTimeIssues(List<RootCause> rootCauses) {
        // Get transactions with high waiting times
        List<Transaction> highWaitingTransactions = performanceAnalyzer.identifyHighWaitingTimeTransactions(95);
        
        if (highWaitingTransactions.isEmpty()) {
            return;
        }
        
        // Calculate average waiting time percentage
        double avgWaitingTimePercentage = highWaitingTransactions.stream()
                .mapToDouble(Transaction::getWaitingTimePercentage)
                .average()
                .orElse(0);
        
        // If waiting time is a significant issue
        if (avgWaitingTimePercentage > 50) {
            // Identify blocking transactions
            Map<String, Integer> blockingKinds = performanceAnalyzer.identifyBlockingTransactions();
            
            // Find the most common blocking transaction kind
            String mostBlockingKind = blockingKinds.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("Unknown");
            
            // Create a root cause for waiting time issues
            RootCause waitingTimeRootCause = new RootCause();
            waitingTimeRootCause.setCategory("Waiting Time");
            waitingTimeRootCause.setDescription("High waiting times due to transaction blocking");
            waitingTimeRootCause.setDetails("Transactions are spending an average of " + 
                    String.format("%.2f", avgWaitingTimePercentage) + 
                    "% of their time waiting. The most common blocking transaction kind is '" + 
                    mostBlockingKind + "'.");
            
            // Calculate impact score based on waiting time percentage and number of affected transactions
            double impactScore = (avgWaitingTimePercentage / 100.0) * 
                    (double) highWaitingTransactions.size() / transactions.size();
            waitingTimeRootCause.setImpactScore(impactScore);
            
            // Add recommendations
            List<String> recommendations = new ArrayList<>();
            recommendations.add("Increase thread pool size to reduce waiting times");
            recommendations.add("Optimize '" + mostBlockingKind + "' transactions to reduce their execution time");
            recommendations.add("Consider implementing transaction prioritization to prevent critical transactions from waiting");
            waitingTimeRootCause.setRecommendations(recommendations);
            
            rootCauses.add(waitingTimeRootCause);
        }
    }
    
    /**
     * Analyzes processing time issues and adds root causes to the list.
     *
     * @param rootCauses the list of root causes to add to
     */
    private void analyzeProcessingTimeIssues(List<RootCause> rootCauses) {
        // Get long-running transactions
        List<Transaction> longRunningTransactions = performanceAnalyzer.identifyLongRunningTransactions(95);
        
        if (longRunningTransactions.isEmpty()) {
            return;
        }
        
        // Group by transaction kind
        Map<String, List<Transaction>> transactionsByKind = longRunningTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getTransactionKind));
        
        // Analyze each transaction kind
        for (Map.Entry<String, List<Transaction>> entry : transactionsByKind.entrySet()) {
            String kind = entry.getKey();
            List<Transaction> kindTransactions = entry.getValue();
            
            // Calculate average processing time
            double avgProcTime = kindTransactions.stream()
                    .mapToDouble(Transaction::getProcTime)
                    .average()
                    .orElse(0);
            
            // Calculate average processing time percentage
            double avgProcTimePercentage = kindTransactions.stream()
                    .mapToDouble(Transaction::getProcTimePercentage)
                    .average()
                    .orElse(0);
            
            // If processing time is a significant issue
            if (avgProcTimePercentage > 30) {
                // Create a root cause for processing time issues
                RootCause procTimeRootCause = new RootCause();
                procTimeRootCause.setCategory("Processing Time");
                procTimeRootCause.setDescription("High processing times in '" + kind + "' transactions");
                procTimeRootCause.setDetails("'" + kind + "' transactions are spending an average of " + 
                        String.format("%.2f", avgProcTimePercentage) + 
                        "% of their time in processing (average " + 
                        String.format("%.2f", avgProcTime) + " ms).");
                
                // Calculate impact score based on processing time percentage and number of affected transactions
                double impactScore = (avgProcTimePercentage / 100.0) * 
                        (double) kindTransactions.size() / transactions.size();
                procTimeRootCause.setImpactScore(impactScore);
                
                // Add recommendations
                List<String> recommendations = new ArrayList<>();
                recommendations.add("Optimize procedural code in '" + kind + "' transactions");
                recommendations.add("Consider implementing caching for frequently accessed data");
                recommendations.add("Review algorithms used in processing to identify inefficiencies");
                procTimeRootCause.setRecommendations(recommendations);
                
                rootCauses.add(procTimeRootCause);
            }
        }
    }
    
    /**
     * Analyzes database time issues and adds root causes to the list.
     *
     * @param rootCauses the list of root causes to add to
     */
    private void analyzeDatabaseTimeIssues(List<RootCause> rootCauses) {
        // Get long-running transactions
        List<Transaction> longRunningTransactions = performanceAnalyzer.identifyLongRunningTransactions(95);
        
        if (longRunningTransactions.isEmpty()) {
            return;
        }
        
        // Group by transaction kind
        Map<String, List<Transaction>> transactionsByKind = longRunningTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getTransactionKind));
        
        // Analyze each transaction kind
        for (Map.Entry<String, List<Transaction>> entry : transactionsByKind.entrySet()) {
            String kind = entry.getKey();
            List<Transaction> kindTransactions = entry.getValue();
            
            // Calculate average database time
            double avgDbTime = kindTransactions.stream()
                    .mapToDouble(Transaction::getDbTime)
                    .average()
                    .orElse(0);
            
            // Calculate average database time percentage
            double avgDbTimePercentage = kindTransactions.stream()
                    .mapToDouble(Transaction::getDbTimePercentage)
                    .average()
                    .orElse(0);
            
            // If database time is a significant issue
            if (avgDbTimePercentage > 30) {
                // Create a root cause for database time issues
                RootCause dbTimeRootCause = new RootCause();
                dbTimeRootCause.setCategory("Database Time");
                dbTimeRootCause.setDescription("High database times in '" + kind + "' transactions");
                dbTimeRootCause.setDetails("'" + kind + "' transactions are spending an average of " + 
                        String.format("%.2f", avgDbTimePercentage) + 
                        "% of their time in database operations (average " + 
                        String.format("%.2f", avgDbTime) + " ms).");
                
                // Calculate impact score based on database time percentage and number of affected transactions
                double impactScore = (avgDbTimePercentage / 100.0) * 
                        (double) kindTransactions.size() / transactions.size();
                dbTimeRootCause.setImpactScore(impactScore);
                
                // Add recommendations
                List<String> recommendations = new ArrayList<>();
                recommendations.add("Optimize database queries in '" + kind + "' transactions");
                recommendations.add("Consider adding appropriate indexes to improve query performance");
                recommendations.add("Implement database connection pooling if not already in use");
                recommendations.add("Review database schema design for potential optimizations");
                dbTimeRootCause.setRecommendations(recommendations);
                
                rootCauses.add(dbTimeRootCause);
            }
        }
    }
    
    /**
     * Analyzes memory issues and adds root causes to the list.
     *
     * @param rootCauses the list of root causes to add to
     */
    private void analyzeMemoryIssues(List<RootCause> rootCauses) {
        // Get potential memory leak sources
        List<MemoryAnalyzer.MemoryLeakSource> potentialLeaks = memoryAnalyzer.identifyPotentialMemoryLeaks();
        
        // Get transactions with abnormal memory growth
        List<Transaction> abnormalMemoryGrowth = memoryAnalyzer.identifyAbnormalMemoryGrowth(95);
        
        // If there are potential memory leaks
        if (!potentialLeaks.isEmpty()) {
            // Get the top memory leak source
            MemoryAnalyzer.MemoryLeakSource topLeakSource = potentialLeaks.get(0);
            
            // Create a root cause for memory leaks
            RootCause memoryLeakRootCause = new RootCause();
            memoryLeakRootCause.setCategory("Memory Leak");
            memoryLeakRootCause.setDescription("Potential memory leak in '" + topLeakSource.getActionName() + "'");
            memoryLeakRootCause.setDetails("Action '" + topLeakSource.getActionName() + 
                    "' shows signs of memory leakage with a risk score of " + 
                    String.format("%.2f", topLeakSource.getRiskScore()) + 
                    " (Risk Level: " + topLeakSource.getRiskLevel() + "). " +
                    "Memory retention rate: " + String.format("%.2f", topLeakSource.getMemoryRetention() * 100) + "%, " +
                    "Memory growth rate: " + String.format("%.2f", topLeakSource.getMemoryGrowthRate()) + " bytes/transaction.");
            
            // Calculate impact score based on risk score and number of affected transactions
            double impactScore = topLeakSource.getRiskScore() * 
                    (double) topLeakSource.getTransactionCount() / transactions.size();
            memoryLeakRootCause.setImpactScore(impactScore);
            
            // Add recommendations
            List<String> recommendations = new ArrayList<>();
            recommendations.add("Review memory management in '" + topLeakSource.getActionName() + "' for potential leaks");
            recommendations.add("Implement proper resource cleanup in finally blocks or try-with-resources");
            recommendations.add("Consider using a memory profiler to identify specific memory leak locations");
            recommendations.add("Increase JVM heap size as a temporary measure while addressing the memory leak");
            memoryLeakRootCause.setRecommendations(recommendations);
            
            rootCauses.add(memoryLeakRootCause);
        }
        
        // If there are transactions with abnormal memory growth
        if (!abnormalMemoryGrowth.isEmpty()) {
            // Group by transaction kind
            Map<String, List<Transaction>> transactionsByKind = abnormalMemoryGrowth.stream()
                    .collect(Collectors.groupingBy(Transaction::getTransactionKind));
            
            // Find the kind with the most transactions
            String mostCommonKind = transactionsByKind.entrySet().stream()
                    .max(Comparator.comparingInt(e -> e.getValue().size()))
                    .map(Map.Entry::getKey)
                    .orElse("Unknown");
            
            List<Transaction> kindTransactions = transactionsByKind.get(mostCommonKind);
            
            // Calculate average memory usage
            double avgMemoryUsage = kindTransactions.stream()
                    .mapToDouble(t -> t.getProcMem() + t.getFuncMem() + t.getDbMem() + t.getStreamMem())
                    .average()
                    .orElse(0);
            
            // Create a root cause for abnormal memory growth
            RootCause memoryGrowthRootCause = new RootCause();
            memoryGrowthRootCause.setCategory("Memory Growth");
            memoryGrowthRootCause.setDescription("Abnormal memory growth in '" + mostCommonKind + "' transactions");
            memoryGrowthRootCause.setDetails("'" + mostCommonKind + "' transactions show abnormal memory growth " +
                    "with an average memory usage of " + String.format("%.2f", avgMemoryUsage) + " bytes. " +
                    "This could lead to memory exhaustion and out-of-memory errors.");
            
            // Calculate impact score based on the number of affected transactions
            double impactScore = 0.8 * (double) kindTransactions.size() / transactions.size();
            memoryGrowthRootCause.setImpactScore(impactScore);
            
            // Add recommendations
            List<String> recommendations = new ArrayList<>();
            recommendations.add("Optimize memory usage in '" + mostCommonKind + "' transactions");
            recommendations.add("Review data structures used and consider more memory-efficient alternatives");
            recommendations.add("Implement memory usage limits or circuit breakers to prevent runaway memory consumption");
            recommendations.add("Consider implementing incremental processing for large data sets");
            memoryGrowthRootCause.setRecommendations(recommendations);
            
            rootCauses.add(memoryGrowthRootCause);
        }
    }
    
    /**
     * Analyzes thread pool issues and adds root causes to the list.
     *
     * @param rootCauses the list of root causes to add to
     */
    private void analyzeThreadPoolIssues(List<RootCause> rootCauses) {
        // Get transactions with high waiting times
        List<Transaction> highWaitingTransactions = performanceAnalyzer.identifyHighWaitingTimeTransactions(95);
        
        if (highWaitingTransactions.isEmpty()) {
            return;
        }
        
        // Calculate correlation between transaction count and waiting time
        double correlation = performanceAnalyzer.calculateTransactionCountWaitingTimeCorrelation();
        
        // If there's a strong positive correlation
        if (correlation > 0.7) {
            // Create a root cause for thread pool issues
            RootCause threadPoolRootCause = new RootCause();
            threadPoolRootCause.setCategory("Thread Pool");
            threadPoolRootCause.setDescription("Insufficient thread pool size");
            threadPoolRootCause.setDetails("There is a strong correlation (" + 
                    String.format("%.2f", correlation) + 
                    ") between the number of concurrent transactions and waiting times, " +
                    "indicating that the thread pool size may be insufficient to handle the load.");
            
            // Calculate impact score based on correlation and number of affected transactions
            double impactScore = correlation * 
                    (double) highWaitingTransactions.size() / transactions.size();
            threadPoolRootCause.setImpactScore(impactScore);
            
            // Add recommendations
            List<String> recommendations = new ArrayList<>();
            recommendations.add("Increase thread pool size to handle concurrent transactions");
            recommendations.add("Implement a dynamic thread pool that can adjust based on load");
            recommendations.add("Consider implementing a thread pool monitoring system");
            recommendations.add("Review thread pool configuration parameters (e.g., queue size, keep-alive time)");
            threadPoolRootCause.setRecommendations(recommendations);
            
            rootCauses.add(threadPoolRootCause);
        }
    }
    
    /**
     * Inner class to represent a root cause of performance issues.
     */
    public static class RootCause {
        private String category;
        private String description;
        private String details;
        private double impactScore;
        private List<String> recommendations;
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getDetails() {
            return details;
        }
        
        public void setDetails(String details) {
            this.details = details;
        }
        
        public double getImpactScore() {
            return impactScore;
        }
        
        public void setImpactScore(double impactScore) {
            this.impactScore = impactScore;
        }
        
        public List<String> getRecommendations() {
            return recommendations;
        }
        
        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations;
        }
        
        /**
         * Gets the impact level based on the impact score.
         *
         * @return the impact level (Critical, High, Medium, or Low)
         */
        public String getImpactLevel() {
            if (impactScore >= 0.8) {
                return "Critical";
            } else if (impactScore >= 0.6) {
                return "High";
            } else if (impactScore >= 0.3) {
                return "Medium";
            } else {
                return "Low";
            }
        }
    }
}
