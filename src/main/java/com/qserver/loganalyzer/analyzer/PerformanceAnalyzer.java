package com.qserver.loganalyzer.analyzer;

import com.qserver.loganalyzer.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * Analyzes transaction data to identify performance bottlenecks.
 * This class provides methods to analyze transaction performance metrics
 * and identify transactions causing CPU bottlenecks and wait times.
 */
public class PerformanceAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceAnalyzer.class);
    
    private List<Transaction> transactions;
    
    /**
     * Constructor that initializes the analyzer with a list of transactions.
     *
     * @param transactions List of transactions to analyze
     */
    public PerformanceAnalyzer(List<Transaction> transactions) {
        this.transactions = transactions;
        logger.info("Performance analyzer initialized with {} transactions", transactions.size());
    }
    
    /**
     * Identifies transactions with the highest CPU usage.
     *
     * @param limit Maximum number of transactions to return
     * @return List of transactions with highest processing time
     */
    public List<Transaction> identifyCpuIntensiveTransactions(int limit) {
        logger.info("Identifying top {} CPU-intensive transactions", limit);
        
        return transactions.stream()
                .sorted(Comparator.comparingDouble(Transaction::getProcessingTime).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Identifies transaction kinds with the highest total CPU usage.
     *
     * @param limit Maximum number of transaction kinds to return
     * @return Map of transaction kinds to their total processing time
     */
    public Map<String, Double> identifyCpuIntensiveTransactionKinds(int limit) {
        logger.info("Identifying top {} CPU-intensive transaction kinds", limit);
        
        Map<String, Double> kindToCpuTime = new HashMap<>();
        
        // Group transactions by kind and sum processing times
        for (Transaction transaction : transactions) {
            String kind = transaction.getKind();
            double processingTime = transaction.getProcessingTime();
            
            kindToCpuTime.put(kind, kindToCpuTime.getOrDefault(kind, 0.0) + processingTime);
        }
        
        // Sort by total processing time and limit results
        return kindToCpuTime.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * Identifies transactions with the highest wait times.
     *
     * @param limit Maximum number of transactions to return
     * @return List of transactions with highest wait time
     */
    public List<Transaction> identifyHighWaitTimeTransactions(int limit) {
        logger.info("Identifying top {} transactions with high wait times", limit);
        
        return transactions.stream()
                .sorted(Comparator.comparingDouble(Transaction::getWaitTime).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Identifies transaction kinds with the highest total wait time.
     *
     * @param limit Maximum number of transaction kinds to return
     * @return Map of transaction kinds to their total wait time
     */
    public Map<String, Double> identifyHighWaitTimeTransactionKinds(int limit) {
        logger.info("Identifying top {} transaction kinds with high wait times", limit);
        
        Map<String, Double> kindToWaitTime = new HashMap<>();
        
        // Group transactions by kind and sum wait times
        for (Transaction transaction : transactions) {
            String kind = transaction.getKind();
            double waitTime = transaction.getWaitTime();
            
            kindToWaitTime.put(kind, kindToWaitTime.getOrDefault(kind, 0.0) + waitTime);
        }
        
        // Sort by total wait time and limit results
        return kindToWaitTime.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * Identifies threads with the highest CPU usage.
     *
     * @param limit Maximum number of threads to return
     * @return Map of thread names to their total processing time
     */
    public Map<String, Double> identifyCpuIntensiveThreads(int limit) {
        logger.info("Identifying top {} CPU-intensive threads", limit);
        
        Map<String, Double> threadToCpuTime = new HashMap<>();
        
        // Group transactions by thread and sum processing times
        for (Transaction transaction : transactions) {
            String thread = transaction.getThread();
            double processingTime = transaction.getProcessingTime();
            
            threadToCpuTime.put(thread, threadToCpuTime.getOrDefault(thread, 0.0) + processingTime);
        }
        
        // Sort by total processing time and limit results
        return threadToCpuTime.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * Calculates the overall wait-to-processing ratio across all transactions.
     *
     * @return Overall wait-to-processing ratio
     */
    public double calculateOverallWaitToProcessingRatio() {
        logger.info("Calculating overall wait-to-processing ratio");
        
        double totalWaitTime = transactions.stream()
                .mapToDouble(Transaction::getWaitTime)
                .sum();
        
        double totalProcessingTime = transactions.stream()
                .mapToDouble(Transaction::getProcessingTime)
                .sum();
        
        if (totalProcessingTime == 0) {
            return 0;
        }
        
        return totalWaitTime / totalProcessingTime;
    }
    
    /**
     * Analyzes resource utilization breakdown across all transactions.
     *
     * @return Map of resource types to their total utilization time
     */
    public Map<String, Double> analyzeResourceUtilization() {
        logger.info("Analyzing resource utilization breakdown");
        
        Map<String, Double> resourceUtilization = new LinkedHashMap<>();
        
        double totalBeginTime = transactions.stream().mapToDouble(Transaction::getBeginTime).sum();
        double totalFuncTime = transactions.stream().mapToDouble(Transaction::getFuncTime).sum();
        double totalDbTime = transactions.stream().mapToDouble(Transaction::getDbTime).sum();
        double totalMemoryCommitTime = transactions.stream().mapToDouble(Transaction::getMemoryCommitTime).sum();
        double totalStreamTime = transactions.stream().mapToDouble(Transaction::getStreamTime).sum();
        double totalKernelTime = transactions.stream().mapToDouble(Transaction::getKernelTime).sum();
        double totalCleanupTime = transactions.stream().mapToDouble(Transaction::getCleanupTime).sum();
        double totalEndProcessTime = transactions.stream().mapToDouble(Transaction::getEndProcessTime).sum();
        double totalProcessingTime = transactions.stream().mapToDouble(Transaction::getProcessingTime).sum();
        
        resourceUtilization.put("Begin", totalBeginTime);
        resourceUtilization.put("Processing", totalProcessingTime);
        resourceUtilization.put("Function", totalFuncTime);
        resourceUtilization.put("Database", totalDbTime);
        resourceUtilization.put("Memory Commit", totalMemoryCommitTime);
        resourceUtilization.put("Stream", totalStreamTime);
        resourceUtilization.put("Kernel", totalKernelTime);
        resourceUtilization.put("Cleanup", totalCleanupTime);
        resourceUtilization.put("End", totalEndProcessTime);
        
        return resourceUtilization;
    }
    
    /**
     * Identifies transactions that are likely causing others to wait.
     * This is determined by correlating high CPU usage with high wait times
     * for other transactions during the same time period.
     *
     * @param limit Maximum number of transactions to return
     * @return List of transactions likely causing others to wait
     */
    public List<Transaction> identifyTransactionsCausingWaits(int limit) {
        logger.info("Identifying top {} transactions causing waits", limit);
        
        // Sort transactions by processing time (CPU usage)
        List<Transaction> sortedByProcessingTime = new ArrayList<>(transactions);
        sortedByProcessingTime.sort(Comparator.comparingDouble(Transaction::getProcessingTime).reversed());
        
        // Create a list to store transactions causing waits
        List<Transaction> transactionsCausingWaits = new ArrayList<>();
        
        // For each high-CPU transaction, count how many other transactions
        // were waiting during its execution time
        for (Transaction highCpuTransaction : sortedByProcessingTime) {
            if (highCpuTransaction.getStartTime() == null || highCpuTransaction.getEndTime() == null) {
                continue;
            }
            
            int waitCount = 0;
            double totalWaitTime = 0.0;
            
            for (Transaction otherTransaction : transactions) {
                if (otherTransaction.equals(highCpuTransaction) || 
                    otherTransaction.getStartTime() == null || 
                    otherTransaction.getWaitTime() <= 0) {
                    continue;
                }
                
                // Check if other transaction was waiting during high-CPU transaction execution
                if (otherTransaction.getStartTime().isAfter(highCpuTransaction.getStartTime()) &&
                    otherTransaction.getStartTime().isBefore(highCpuTransaction.getEndTime())) {
                    waitCount++;
                    totalWaitTime += otherTransaction.getWaitTime();
                }
            }
            
            // If this transaction caused waits, add it to the list
            if (waitCount > 0) {
                highCpuTransaction.setDetails("Caused " + waitCount + " waits, total wait time: " + totalWaitTime);
                transactionsCausingWaits.add(highCpuTransaction);
                
                if (transactionsCausingWaits.size() >= limit) {
                    break;
                }
            }
        }
        
        return transactionsCausingWaits;
    }
    
    /**
     * Calculates the number of active threads based on unique thread names.
     *
     * @return Number of active threads
     */
    public int calculateActiveThreadCount() {
        logger.info("Calculating active thread count");
        
        Set<String> uniqueThreads = transactions.stream()
                .map(Transaction::getThread)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        return uniqueThreads.size();
    }
    
    /**
     * Calculates the maximum number of concurrent transactions.
     *
     * @return Maximum number of concurrent transactions
     */
    public int calculateMaxConcurrentTransactions() {
        logger.info("Calculating maximum concurrent transactions");
        
        // Filter transactions with valid start and end times
        List<Transaction> validTransactions = transactions.stream()
                .filter(t -> t.getStartTime() != null && t.getEndTime() != null)
                .collect(Collectors.toList());
        
        if (validTransactions.isEmpty()) {
            return 0;
        }
        
        // Create a list of transaction start and end events
        List<TransactionEvent> events = new ArrayList<>();
        
        for (Transaction transaction : validTransactions) {
            events.add(new TransactionEvent(transaction.getStartTime(), true));
            events.add(new TransactionEvent(transaction.getEndTime(), false));
        }
        
        // Sort events by time
        events.sort(Comparator.comparing(TransactionEvent::getTime));
        
        // Count concurrent transactions
        int currentConcurrent = 0;
        int maxConcurrent = 0;
        
        for (TransactionEvent event : events) {
            if (event.isStart()) {
                currentConcurrent++;
            } else {
                currentConcurrent--;
            }
            
            maxConcurrent = Math.max(maxConcurrent, currentConcurrent);
        }
        
        return maxConcurrent;
    }
    
    /**
     * Helper class to represent transaction start and end events.
     */
    private static class TransactionEvent {
        private final LocalDateTime time;
        private final boolean isStart;
        
        public TransactionEvent(LocalDateTime time, boolean isStart) {
            this.time = time;
            this.isStart = isStart;
        }
        
        public LocalDateTime getTime() {
            return time;
        }
        
        public boolean isStart() {
            return isStart;
        }
    }
}
