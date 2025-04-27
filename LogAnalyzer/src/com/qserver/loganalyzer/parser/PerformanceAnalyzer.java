package com.qserver.loganalyzer.analyzer;

import com.qserver.loganalyzer.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Analyzes transaction data to identify performance bottlenecks and issues.
 * This class provides methods for analyzing various aspects of transaction performance.
 */
public class PerformanceAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceAnalyzer.class);
    
    private final List<Transaction> transactions;
    
    /**
     * Constructs a new PerformanceAnalyzer with the given transactions.
     *
     * @param transactions the list of transactions to analyze
     */
    public PerformanceAnalyzer(List<Transaction> transactions) {
        this.transactions = new ArrayList<>(transactions);
        logger.info("Initialized PerformanceAnalyzer with {} transactions", transactions.size());
    }
    
    /**
     * Gets the list of finished transactions.
     *
     * @return the list of finished transactions
     */
    public List<Transaction> getFinishedTransactions() {
        return transactions.stream()
                .filter(t -> "Finished".equals(t.getStatus()))
                .collect(Collectors.toList());
    }
    
    /**
     * Identifies long-running transactions based on a percentile threshold.
     *
     * @param percentileThreshold the percentile threshold (e.g., 95 for 95th percentile)
     * @return the list of long-running transactions
     */
    public List<Transaction> identifyLongRunningTransactions(double percentileThreshold) {
        List<Transaction> finishedTransactions = getFinishedTransactions();
        
        // Calculate the percentile threshold value
        List<Long> lengths = finishedTransactions.stream()
                .map(Transaction::getLength)
                .sorted()
                .collect(Collectors.toList());
        
        int index = (int) Math.ceil(percentileThreshold / 100.0 * lengths.size()) - 1;
        if (index < 0) index = 0;
        if (index >= lengths.size()) index = lengths.size() - 1;
        
        long thresholdValue = lengths.get(index);
        logger.info("Long-running transaction threshold ({}th percentile): {} ms", 
                percentileThreshold, thresholdValue);
        
        // Filter transactions above the threshold
        List<Transaction> longRunning = finishedTransactions.stream()
                .filter(t -> t.getLength() > thresholdValue)
                .collect(Collectors.toList());
        
        logger.info("Identified {} long-running transactions", longRunning.size());
        return longRunning;
    }
    
    /**
     * Identifies transactions with high waiting times.
     *
     * @param percentileThreshold the percentile threshold (e.g., 95 for 95th percentile)
     * @return the list of transactions with high waiting times
     */
    public List<Transaction> identifyHighWaitingTimeTransactions(double percentileThreshold) {
        List<Transaction> finishedTransactions = getFinishedTransactions();
        
        // Calculate the percentile threshold value
        List<Long> waitingTimes = finishedTransactions.stream()
                .map(Transaction::getWaitingTime)
                .sorted()
                .collect(Collectors.toList());
        
        int index = (int) Math.ceil(percentileThreshold / 100.0 * waitingTimes.size()) - 1;
        if (index < 0) index = 0;
        if (index >= waitingTimes.size()) index = waitingTimes.size() - 1;
        
        long thresholdValue = waitingTimes.get(index);
        logger.info("High waiting time threshold ({}th percentile): {} ms", 
                percentileThreshold, thresholdValue);
        
        // Filter transactions above the threshold
        List<Transaction> highWaiting = finishedTransactions.stream()
                .filter(t -> t.getWaitingTime() > thresholdValue)
                .collect(Collectors.toList());
        
        logger.info("Identified {} transactions with high waiting times", highWaiting.size());
        return highWaiting;
    }
    
    /**
     * Identifies transactions with high memory usage.
     *
     * @param percentileThreshold the percentile threshold (e.g., 95 for 95th percentile)
     * @return the list of transactions with high memory usage
     */
    public List<Transaction> identifyHighMemoryUsageTransactions(double percentileThreshold) {
        List<Transaction> finishedTransactions = getFinishedTransactions();
        
        // Calculate total memory usage for each transaction
        Map<Transaction, Long> totalMemoryMap = finishedTransactions.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        t -> t.getProcMem() + t.getFuncMem() + t.getDbMem() + t.getStreamMem()
                ));
        
        // Calculate the percentile threshold value
        List<Long> totalMemories = new ArrayList<>(totalMemoryMap.values());
        Collections.sort(totalMemories);
        
        int index = (int) Math.ceil(percentileThreshold / 100.0 * totalMemories.size()) - 1;
        if (index < 0) index = 0;
        if (index >= totalMemories.size()) index = totalMemories.size() - 1;
        
        long thresholdValue = totalMemories.get(index);
        logger.info("High memory usage threshold ({}th percentile): {} bytes", 
                percentileThreshold, thresholdValue);
        
        // Filter transactions above the threshold
        List<Transaction> highMemory = totalMemoryMap.entrySet().stream()
                .filter(entry -> entry.getValue() > thresholdValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        logger.info("Identified {} transactions with high memory usage", highMemory.size());
        return highMemory;
    }
    
    /**
     * Analyzes transaction performance by transaction kind.
     *
     * @return a map of transaction kind to performance statistics
     */
    public Map<String, TransactionStats> analyzeByTransactionKind() {
        List<Transaction> finishedTransactions = getFinishedTransactions();
        
        Map<String, TransactionStats> kindStats = new HashMap<>();
        
        // Group transactions by kind
        Map<String, List<Transaction>> transactionsByKind = finishedTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getTransactionKind));
        
        // Calculate statistics for each kind
        for (Map.Entry<String, List<Transaction>> entry : transactionsByKind.entrySet()) {
            String kind = entry.getKey();
            List<Transaction> kindTransactions = entry.getValue();
            
            TransactionStats stats = calculateStats(kindTransactions);
            kindStats.put(kind, stats);
        }
        
        return kindStats;
    }
    
    /**
     * Analyzes transaction performance by action element name.
     *
     * @return a map of action element name to performance statistics
     */
    public Map<String, TransactionStats> analyzeByActionElementName() {
        List<Transaction> finishedTransactions = getFinishedTransactions();
        
        Map<String, TransactionStats> actionStats = new HashMap<>();
        
        // Group transactions by action element name
        Map<String, List<Transaction>> transactionsByAction = finishedTransactions.stream()
                .filter(t -> t.getActionElementName() != null && !t.getActionElementName().isEmpty())
                .collect(Collectors.groupingBy(Transaction::getActionElementName));
        
        // Calculate statistics for each action
        for (Map.Entry<String, List<Transaction>> entry : transactionsByAction.entrySet()) {
            String action = entry.getKey();
            List<Transaction> actionTransactions = entry.getValue();
            
            TransactionStats stats = calculateStats(actionTransactions);
            actionStats.put(action, stats);
        }
        
        return actionStats;
    }
    
    /**
     * Analyzes transaction performance by thread name.
     *
     * @return a map of thread name to performance statistics
     */
    public Map<String, TransactionStats> analyzeByThreadName() {
        List<Transaction> finishedTransactions = getFinishedTransactions();
        
        Map<String, TransactionStats> threadStats = new HashMap<>();
        
        // Group transactions by thread name
        Map<String, List<Transaction>> transactionsByThread = finishedTransactions.stream()
                .filter(t -> t.getThreadName() != null && !t.getThreadName().isEmpty())
                .collect(Collectors.groupingBy(Transaction::getThreadName));
        
        // Calculate statistics for each thread
        for (Map.Entry<String, List<Transaction>> entry : transactionsByThread.entrySet()) {
            String thread = entry.getKey();
            List<Transaction> threadTransactions = entry.getValue();
            
            TransactionStats stats = calculateStats(threadTransactions);
            threadStats.put(thread, stats);
        }
        
        return threadStats;
    }
    
    /**
     * Analyzes transaction performance by hour of day.
     *
     * @return a map of hour to performance statistics
     */
    public Map<Integer, TransactionStats> analyzeByHourOfDay() {
        List<Transaction> finishedTransactions = getFinishedTransactions();
        
        Map<Integer, TransactionStats> hourStats = new HashMap<>();
        
        // Group transactions by hour of day
        Map<Integer, List<Transaction>> transactionsByHour = finishedTransactions.stream()
                .filter(t -> t.getStartTime() != null)
                .collect(Collectors.groupingBy(t -> t.getStartTime().getHour()));
        
        // Calculate statistics for each hour
        for (Map.Entry<Integer, List<Transaction>> entry : transactionsByHour.entrySet()) {
            Integer hour = entry.getKey();
            List<Transaction> hourTransactions = entry.getValue();
            
            TransactionStats stats = calculateStats(hourTransactions);
            hourStats.put(hour, stats);
        }
        
        return hourStats;
    }
    
    /**
     * Identifies transactions that are causing others to wait.
     *
     * @return a map of transaction kind to the count of times it was found to be blocking
     */
    public Map<String, Integer> identifyBlockingTransactions() {
        List<Transaction> finishedTransactions = getFinishedTransactions();
        List<Transaction> highWaitingTransactions = identifyHighWaitingTimeTransactions(95);
        
        Map<String, Integer> blockingKinds = new HashMap<>();
        
        // For each high waiting time transaction, find transactions that were running when it started
        for (Transaction waitingTrans : highWaitingTransactions) {
            if (waitingTrans.getStartTime() == null) continue;
            
            // Find transactions that were running when this one started
            List<Transaction> runningTransactions = finishedTransactions.stream()
                    .filter(t -> t.getStartTime() != null && t.getEndTime() != null)
                    .filter(t -> !t.getTransactionId().equals(waitingTrans.getTransactionId()))
                    .filter(t -> t.getStartTime().isBefore(waitingTrans.getStartTime()) && 
                                 t.getEndTime().isAfter(waitingTrans.getStartTime()))
                    .collect(Collectors.toList());
            
            // Count by transaction kind
            for (Transaction runningTrans : runningTransactions) {
                String kind = runningTrans.getTransactionKind();
                blockingKinds.put(kind, blockingKinds.getOrDefault(kind, 0) + 1);
            }
        }
        
        return blockingKinds;
    }
    
    /**
     * Calculates the correlation between transaction count and waiting time.
     *
     * @return the correlation coefficient
     */
    public double calculateTransactionCountWaitingTimeCorrelation() {
        List<Transaction> finishedTransactions = getFinishedTransactions();
        
        // Group transactions by minute
        Map<String, List<Transaction>> transactionsByMinute = finishedTransactions.stream()
                .filter(t -> t.getStartTime() != null)
                .collect(Collectors.groupingBy(t -> {
                    LocalDateTime dt = t.getStartTime();
                    return String.format("%04d-%02d-%02d %02d:%02d", 
                            dt.getYear(), dt.getMonthValue(), dt.getDayOfMonth(), 
                            dt.getHour(), dt.getMinute());
                }));
        
        // Calculate transaction count and mean waiting time for each minute
        List<Integer> counts = new ArrayList<>();
        List<Double> meanWaitingTimes = new ArrayList<>();
        
        for (List<Transaction> minuteTransactions : transactionsByMinute.values()) {
            counts.add(minuteTransactions.size());
            
            double meanWaitingTime = minuteTransactions.stream()
                    .mapToLong(Transaction::getWaitingTime)
                    .average()
                    .orElse(0);
            
            meanWaitingTimes.add(meanWaitingTime);
        }
        
        // Calculate correlation
        return calculateCorrelation(counts, meanWaitingTimes);
    }
    
    /**
     * Calculates performance statistics for a list of transactions.
     *
     * @param transactions the list of transactions
     * @return the calculated statistics
     */
    private TransactionStats calculateStats(List<Transaction> transactions) {
        TransactionStats stats = new TransactionStats();
        stats.setCount(transactions.size());
        
        // Calculate length statistics
        DoubleSummaryStatistics lengthStats = transactions.stream()
                .mapToDouble(Transaction::getLength)
                .summaryStatistics();
        
        stats.setMeanLength(lengthStats.getAverage());
        stats.setMinLength(lengthStats.getMin());
        stats.setMaxLength(lengthStats.getMax());
        stats.setTotalLength(lengthStats.getSum());
        
        // Calculate waiting time statistics
        DoubleSummaryStatistics waitingStats = transactions.stream()
                .mapToDouble(Transaction::getWaitingTime)
                .summaryStatistics();
        
        stats.setMeanWaitingTime(waitingStats.getAverage());
        stats.setMinWaitingTime(waitingStats.getMin());
        stats.setMaxWaitingTime(waitingStats.getMax());
        stats.setTotalWaitingTime(waitingStats.getSum());
        
        // Calculate processing time statistics
        DoubleSummaryStatistics procStats = transactions.stream()
                .mapToDouble(Transaction::getProcTime)
                .summaryStatistics();
        
        stats.setMeanProcTime(procStats.getAverage());
        stats.setMinProcTime(procStats.getMin());
        stats.setMaxProcTime(procStats.getMax());
        stats.setTotalProcTime(procStats.getSum());
        
        // Calculate functional time statistics
        DoubleSummaryStatistics funcStats = transactions.stream()
                .mapToDouble(Transaction::getFuncTime)
                .summaryStatistics();
        
        stats.setMeanFuncTime(funcStats.getAverage());
        stats.setMinFuncTime(funcStats.getMin());
        stats.setMaxFuncTime(funcStats.getMax());
        stats.setTotalFuncTime(funcStats.getSum());
        
        // Calculate database time statistics
        DoubleSummaryStatistics dbStats = transactions.stream()
                .mapToDouble(Transaction::getDbTime)
                .summaryStatistics();
        
        stats.setMeanDbTime(dbStats.getAverage());
        stats.setMinDbTime(dbStats.getMin());
        stats.setMaxDbTime(dbStats.getMax());
        stats.setTotalDbTime(dbStats.getSum());
        
        // Calculate stream time statistics
        DoubleSummaryStatistics streamStats = transactions.stream()
                .mapToDouble(Transaction::getStreamTime)
                .summaryStatistics();
        
        stats.setMeanStreamTime(streamStats.getAverage());
        stats.setMinStreamTime(streamStats.getMin());
        stats.setMaxStreamTime(streamStats.getMax());
        stats.setTotalStreamTime(streamStats.getSum());
        
        // Calculate waiting time percentage
        stats.setWaitingTimePercentage(stats.getMeanWaitingTime() / stats.getMeanLength() * 100);
        
        // Count dominant time components
        Map<String, Long> dominantComponents = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getDominantTimeComponent,
                        Collectors.counting()
                ));
        
        stats.setDominantComponents(dominantComponents);
        
        return stats;
    }
    
    /**
     * Calculates the correlation coefficient between two lists of numbers.
     *
     * @param x the first list of numbers
     * @param y the second list of numbers
     * @return the correlation coefficient
     */
    private double calculateCorrelation(List<? extends Number> x, List<? extends Number> y) {
        if (x.size() != y.size() || x.isEmpty()) {
            return 0;
        }
        
        int n = x.size();
        
        // Calculate means
        double meanX = x.stream().mapToDouble(Number::doubleValue).average().orElse(0);
        double meanY = y.stream().mapToDouble(Number::doubleValue).average().orElse(0);
        
        // Calculate covariance and variances
        double covariance = 0;
        double varianceX = 0;
        double varianceY = 0;
        
        for (int i = 0; i < n; i++) {
            double xDiff = x.get(i).doubleValue() - meanX;
            double yDiff = y.get(i).doubleValue() - meanY;
            
            covariance += xDiff * yDiff;
            varianceX += xDiff * xDiff;
            varianceY += yDiff * yDiff;
        }
        
        // Calculate correlation coefficient
        if (varianceX == 0 || varianceY == 0) {
            return 0;
        }
        
        return covariance / Math.sqrt(varianceX * varianceY);
    }
    
    /**
     * Inner class to hold transaction performance statistics.
     */
    public static class TransactionStats {
        private int count;
        private double meanLength;
        private double minLength;
        private double maxLength;
        private double totalLength;
        private double meanWaitingTime;
        private double minWaitingTime;
        private double maxWaitingTime;
        private double totalWaitingTime;
        private double meanProcTime;
        private double minProcTime;
        private double maxProcTime;
        private double totalProcTime;
        private double meanFuncTime;
        private double minFuncTime;
        private double maxFuncTime;
        private double totalFuncTime;
        private double meanDbTime;
        private double minDbTime;
        private double maxDbTime;
        private double totalDbTime;
        private double meanStreamTime;
        private double minStreamTime;
        private double maxStreamTime;
        private double totalStreamTime;
        private double waitingTimePercentage;
        private Map<String, Long> dominantComponents;
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public double getMeanLength() {
            return meanLength;
        }
        
        public void setMeanLength(double meanLength) {
            this.meanLength = meanLength;
        }
        
        public double getMinLength() {
            return minLength;
        }
        
        public void setMinLength(double minLength) {
            this.minLength = minLength;
        }
        
        public double getMaxLength() {
            return maxLength;
        }
        
        public void setMaxLength(double maxLength) {
            this.maxLength = maxLength;
        }
        
        public double getTotalLength() {
            return totalLength;
        }
        
        public void setTotalLength(double totalLength) {
            this.totalLength = totalLength;
        }
        
        public double getMeanWaitingTime() {
            return meanWaitingTime;
        }
        
        public void setMeanWaitingTime(double meanWaitingTime) {
            this.meanWaitingTime = meanWaitingTime;
        }
        
        public double getMinWaitingTime() {
            return minWaitingTime;
        }
        
        public void setMinWaitingTime(double minWaitingTime) {
            this.minWaitingTime = minWaitingTime;
        }
        
        public double getMaxWaitingTime() {
            return maxWaitingTime;
        }
        
        public void setMaxWaitingTime(double maxWaitingTime) {
            this.maxWaitingTime = maxWaitingTime;
        }
        
        public double getTotalWaitingTime() {
            return totalWaitingTime;
        }
        
        public void setTotalWaitingTime(double totalWaitingTime) {
            this.totalWaitingTime = totalWaitingTime;
        }
        
        public double getMeanProcTime() {
            return meanProcTime;
        }
        
        public void setMeanProcTime(double meanProcTime) {
            this.meanProcTime = meanProcTime;
        }
        
        public double getMinProcTime() {
            return minProcTime;
        }
        
        public void setMinProcTime(double minProcTime) {
            this.minProcTime = minProcTime;
        }
        
        public double getMaxProcTime() {
            return maxProcTime;
        }
        
        public void setMaxProcTime(double maxProcTime) {
            this.maxProcTime = maxProcTime;
        }
        
        public double getTotalProcTime() {
            return totalProcTime;
        }
        
        public void setTotalProcTime(double totalProcTime) {
            this.totalProcTime = totalProcTime;
        }
        
        public double getMeanFuncTime() {
            return meanFuncTime;
        }
        
        public void setMeanFuncTime(double meanFuncTime) {
            this.meanFuncTime = meanFuncTime;
        }
        
        public double getMinFuncTime() {
            return minFuncTime;
        }
        
        public void setMinFuncTime(double minFuncTime) {
            this.minFuncTime = minFuncTime;
        }
        
        public double getMaxFuncTime() {
            return maxFuncTime;
        }
        
        public void setMaxFuncTime(double maxFuncTime) {
            this.maxFuncTime = maxFuncTime;
        }
        
        public double getTotalFuncTime() {
            return totalFuncTime;
        }
        
        public void setTotalFuncTime(double totalFuncTime) {
            this.totalFuncTime = totalFuncTime;
        }
        
        public double getMeanDbTime() {
            return meanDbTime;
        }
        
        public void setMeanDbTime(double meanDbTime) {
            this.meanDbTime = meanDbTime;
        }
        
        public double getMinDbTime() {
            return minDbTime;
        }
        
        public void setMinDbTime(double minDbTime) {
            this.minDbTime = minDbTime;
        }
        
        public double getMaxDbTime() {
            return maxDbTime;
        }
        
        public void setMaxDbTime(double maxDbTime) {
            this.maxDbTime = maxDbTime;
        }
        
        public double getTotalDbTime() {
            return totalDbTime;
        }
        
        public void setTotalDbTime(double totalDbTime) {
            this.totalDbTime = totalDbTime;
        }
        
        public double getMeanStreamTime() {
            return meanStreamTime;
        }
        
        public void setMeanStreamTime(double meanStreamTime) {
            this.meanStreamTime = meanStreamTime;
        }
        
        public double getMinStreamTime() {
            return minStreamTime;
        }
        
        public void setMinStreamTime(double minStreamTime) {
            this.minStreamTime = minStreamTime;
        }
        
        public double getMaxStreamTime() {
            return maxStreamTime;
        }
        
        public void setMaxStreamTime(double maxStreamTime) {
            this.maxStreamTime = maxStreamTime;
        }
        
        public double getTotalStreamTime() {
            return totalStreamTime;
        }
        
        public void setTotalStreamTime(double totalStreamTime) {
            this.totalStreamTime = totalStreamTime;
        }
        
        public double getWaitingTimePercentage() {
            return waitingTimePercentage;
        }
        
        public void setWaitingTimePercentage(double waitingTimePercentage) {
            this.waitingTimePercentage = waitingTimePercentage;
        }
        
        public Map<String, Long> getDominantComponents() {
            return dominantComponents;
        }
        
        public void setDominantComponents(Map<String, Long> dominantComponents) {
            this.dominantComponents = dominantComponents;
        }
        
        /**
         * Gets the most common dominant time component.
         *
         * @return the most common dominant time component
         */
        public String getMostCommonDominantComponent() {
            if (dominantComponents == null || dominantComponents.isEmpty()) {
                return "unknown";
            }
            
            return dominantComponents.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("unknown");
        }
    }
}
