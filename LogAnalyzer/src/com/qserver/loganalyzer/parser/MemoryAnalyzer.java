package com.qserver.loganalyzer.analyzer;

import com.qserver.loganalyzer.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Analyzes memory usage patterns in transaction data to identify memory leaks and issues.
 * This class provides methods for analyzing various aspects of memory usage in transactions.
 */
public class MemoryAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(MemoryAnalyzer.class);
    
    private final List<Transaction> transactions;
    
    /**
     * Constructs a new MemoryAnalyzer with the given transactions.
     *
     * @param transactions the list of transactions to analyze
     */
    public MemoryAnalyzer(List<Transaction> transactions) {
        this.transactions = new ArrayList<>(transactions);
        logger.info("Initialized MemoryAnalyzer with {} transactions", transactions.size());
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
     * Analyzes memory usage trends over time.
     *
     * @return a map of timestamp to memory usage statistics
     */
    public Map<String, MemoryStats> analyzeMemoryTrendsOverTime() {
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
        
        // Sort timestamps
        List<String> sortedTimestamps = new ArrayList<>(transactionsByMinute.keySet());
        Collections.sort(sortedTimestamps);
        
        // Calculate memory statistics for each minute
        Map<String, MemoryStats> memoryTrends = new LinkedHashMap<>();
        
        for (String timestamp : sortedTimestamps) {
            List<Transaction> minuteTransactions = transactionsByMinute.get(timestamp);
            
            MemoryStats stats = calculateMemoryStats(minuteTransactions);
            memoryTrends.put(timestamp, stats);
        }
        
        return memoryTrends;
    }
    
    /**
     * Identifies transactions with abnormal memory growth rates.
     *
     * @param percentileThreshold the percentile threshold (e.g., 95 for 95th percentile)
     * @return the list of transactions with abnormal memory growth
     */
    public List<Transaction> identifyAbnormalMemoryGrowth(double percentileThreshold) {
        List<Transaction> finishedTransactions = getFinishedTransactions();
        
        // Calculate memory growth rate for each transaction
        Map<Transaction, Double> memoryGrowthRates = new HashMap<>();
        
        for (Transaction transaction : finishedTransactions) {
            // Calculate total memory usage
            long totalMemory = transaction.getProcMem() + transaction.getFuncMem() + 
                               transaction.getDbMem() + transaction.getStreamMem();
            
            // Calculate memory growth rate (bytes per millisecond)
            double growthRate = transaction.getLength() > 0 ? 
                    (double) totalMemory / transaction.getLength() : 0;
            
            memoryGrowthRates.put(transaction, growthRate);
        }
        
        // Calculate the percentile threshold value
        List<Double> rates = new ArrayList<>(memoryGrowthRates.values());
        Collections.sort(rates);
        
        int index = (int) Math.ceil(percentileThreshold / 100.0 * rates.size()) - 1;
        if (index < 0) index = 0;
        if (index >= rates.size()) index = rates.size() - 1;
        
        double thresholdValue = rates.get(index);
        logger.info("Abnormal memory growth rate threshold ({}th percentile): {} bytes/ms", 
                percentileThreshold, thresholdValue);
        
        // Filter transactions above the threshold
        List<Transaction> abnormalGrowth = memoryGrowthRates.entrySet().stream()
                .filter(entry -> entry.getValue() > thresholdValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        logger.info("Identified {} transactions with abnormal memory growth", abnormalGrowth.size());
        return abnormalGrowth;
    }
    
    /**
     * Analyzes memory usage by transaction kind.
     *
     * @return a map of transaction kind to memory statistics
     */
    public Map<String, MemoryStats> analyzeMemoryByTransactionKind() {
        List<Transaction> finishedTransactions = getFinishedTransactions();
        
        Map<String, MemoryStats> kindStats = new HashMap<>();
        
        // Group transactions by kind
        Map<String, List<Transaction>> transactionsByKind = finishedTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getTransactionKind));
        
        // Calculate memory statistics for each kind
        for (Map.Entry<String, List<Transaction>> entry : transactionsByKind.entrySet()) {
            String kind = entry.getKey();
            List<Transaction> kindTransactions = entry.getValue();
            
            MemoryStats stats = calculateMemoryStats(kindTransactions);
            kindStats.put(kind, stats);
        }
        
        return kindStats;
    }
    
    /**
     * Analyzes memory usage by action element name.
     *
     * @return a map of action element name to memory statistics
     */
    public Map<String, MemoryStats> analyzeMemoryByActionElementName() {
        List<Transaction> finishedTransactions = getFinishedTransactions();
        
        Map<String, MemoryStats> actionStats = new HashMap<>();
        
        // Group transactions by action element name
        Map<String, List<Transaction>> transactionsByAction = finishedTransactions.stream()
                .filter(t -> t.getActionElementName() != null && !t.getActionElementName().isEmpty())
                .collect(Collectors.groupingBy(Transaction::getActionElementName));
        
        // Calculate memory statistics for each action
        for (Map.Entry<String, List<Transaction>> entry : transactionsByAction.entrySet()) {
            String action = entry.getKey();
            List<Transaction> actionTransactions = entry.getValue();
            
            MemoryStats stats = calculateMemoryStats(actionTransactions);
            actionStats.put(action, stats);
        }
        
        return actionStats;
    }
    
    /**
     * Identifies potential memory leaks by analyzing memory usage patterns.
     *
     * @return a list of potential memory leak sources with their statistics
     */
    public List<MemoryLeakSource> identifyPotentialMemoryLeaks() {
        Map<String, MemoryStats> actionStats = analyzeMemoryByActionElementName();
        
        List<MemoryLeakSource> potentialLeaks = new ArrayList<>();
        
        // Analyze each action for memory leak patterns
        for (Map.Entry<String, MemoryStats> entry : actionStats.entrySet()) {
            String action = entry.getKey();
            MemoryStats stats = entry.getValue();
            
            // Calculate memory efficiency (bytes per ms)
            double memoryEfficiency = stats.getMeanTotalMemory() / stats.getMeanLength();
            
            // Calculate memory retention (how much memory is not freed)
            double memoryRetention = 1.0 - (stats.getMeanFreeMemory() / stats.getMeanOsVmSize());
            
            // Calculate memory growth rate
            double memoryGrowthRate = stats.getMeanTotalMemory() / stats.getCount();
            
            // Create a memory leak source if it meets the criteria
            if (memoryEfficiency > 1000 || memoryRetention > 0.8 || memoryGrowthRate > 10000) {
                MemoryLeakSource leakSource = new MemoryLeakSource();
                leakSource.setActionName(action);
                leakSource.setTransactionCount(stats.getCount());
                leakSource.setMeanTotalMemory(stats.getMeanTotalMemory());
                leakSource.setMemoryEfficiency(memoryEfficiency);
                leakSource.setMemoryRetention(memoryRetention);
                leakSource.setMemoryGrowthRate(memoryGrowthRate);
                leakSource.setRiskScore(calculateMemoryLeakRiskScore(memoryEfficiency, memoryRetention, memoryGrowthRate));
                
                potentialLeaks.add(leakSource);
            }
        }
        
        // Sort by risk score (descending)
        potentialLeaks.sort(Comparator.comparing(MemoryLeakSource::getRiskScore).reversed());
        
        logger.info("Identified {} potential memory leak sources", potentialLeaks.size());
        return potentialLeaks;
    }
    
    /**
     * Analyzes memory allocation and deallocation patterns.
     *
     * @return a map of transaction kind to memory allocation statistics
     */
    public Map<String, MemoryAllocationStats> analyzeMemoryAllocationPatterns() {
        List<Transaction> finishedTransactions = getFinishedTransactions();
        
        Map<String, MemoryAllocationStats> allocationStats = new HashMap<>();
        
        // Group transactions by kind
        Map<String, List<Transaction>> transactionsByKind = finishedTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getTransactionKind));
        
        // Calculate memory allocation statistics for each kind
        for (Map.Entry<String, List<Transaction>> entry : transactionsByKind.entrySet()) {
            String kind = entry.getKey();
            List<Transaction> kindTransactions = entry.getValue();
            
            MemoryAllocationStats stats = calculateMemoryAllocationStats(kindTransactions);
            allocationStats.put(kind, stats);
        }
        
        return allocationStats;
    }
    
    /**
     * Calculates memory statistics for a list of transactions.
     *
     * @param transactions the list of transactions
     * @return the calculated memory statistics
     */
    private MemoryStats calculateMemoryStats(List<Transaction> transactions) {
        MemoryStats stats = new MemoryStats();
        stats.setCount(transactions.size());
        
        // Calculate total memory statistics
        DoubleSummaryStatistics totalMemoryStats = transactions.stream()
                .mapToDouble(t -> t.getProcMem() + t.getFuncMem() + t.getDbMem() + t.getStreamMem())
                .summaryStatistics();
        
        stats.setMeanTotalMemory(totalMemoryStats.getAverage());
        stats.setMinTotalMemory(totalMemoryStats.getMin());
        stats.setMaxTotalMemory(totalMemoryStats.getMax());
        
        // Calculate processing memory statistics
        DoubleSummaryStatistics procMemStats = transactions.stream()
                .mapToDouble(Transaction::getProcMem)
                .summaryStatistics();
        
        stats.setMeanProcMem(procMemStats.getAverage());
        stats.setMinProcMem(procMemStats.getMin());
        stats.setMaxProcMem(procMemStats.getMax());
        
        // Calculate functional memory statistics
        DoubleSummaryStatistics funcMemStats = transactions.stream()
                .mapToDouble(Transaction::getFuncMem)
                .summaryStatistics();
        
        stats.setMeanFuncMem(funcMemStats.getAverage());
        stats.setMinFuncMem(funcMemStats.getMin());
        stats.setMaxFuncMem(funcMemStats.getMax());
        
        // Calculate database memory statistics
        DoubleSummaryStatistics dbMemStats = transactions.stream()
                .mapToDouble(Transaction::getDbMem)
                .summaryStatistics();
        
        stats.setMeanDbMem(dbMemStats.getAverage());
        stats.setMinDbMem(dbMemStats.getMin());
        stats.setMaxDbMem(dbMemStats.getMax());
        
        // Calculate stream memory statistics
        DoubleSummaryStatistics streamMemStats = transactions.stream()
                .mapToDouble(Transaction::getStreamMem)
                .summaryStatistics();
        
        stats.setMeanStreamMem(streamMemStats.getAverage());
        stats.setMinStreamMem(streamMemStats.getMin());
        stats.setMaxStreamMem(streamMemStats.getMax());
        
        // Calculate OS VM size statistics
        DoubleSummaryStatistics osVmSizeStats = transactions.stream()
                .mapToDouble(Transaction::getOsVmSize)
                .summaryStatistics();
        
        stats.setMeanOsVmSize(osVmSizeStats.getAverage());
        stats.setMinOsVmSize(osVmSizeStats.getMin());
        stats.setMaxOsVmSize(osVmSizeStats.getMax());
        
        // Calculate free memory statistics
        DoubleSummaryStatistics freeMemoryStats = transactions.stream()
                .mapToDouble(Transaction::getFreeMemory)
                .summaryStatistics();
        
        stats.setMeanFreeMemory(freeMemoryStats.getAverage());
        stats.setMinFreeMemory(freeMemoryStats.getMin());
        stats.setMaxFreeMemory(freeMemoryStats.getMax());
        
        // Calculate transaction length statistics
        DoubleSummaryStatistics lengthStats = transactions.stream()
                .mapToDouble(Transaction::getLength)
                .summaryStatistics();
        
        stats.setMeanLength(lengthStats.getAverage());
        stats.setMinLength(lengthStats.getMin());
        stats.setMaxLength(lengthStats.getMax());
        
        return stats;
    }
    
    /**
     * Calculates memory allocation statistics for a list of transactions.
     *
     * @param transactions the list of transactions
     * @return the calculated memory allocation statistics
     */
    private MemoryAllocationStats calculateMemoryAllocationStats(List<Transaction> transactions) {
        MemoryAllocationStats stats = new MemoryAllocationStats();
        stats.setTransactionCount(transactions.size());
        
        // Calculate total memory allocation
        double totalMemoryAllocated = transactions.stream()
                .mapToDouble(t -> t.getProcMem() + t.getFuncMem() + t.getDbMem() + t.getStreamMem())
                .sum();
        
        stats.setTotalMemoryAllocated(totalMemoryAllocated);
        
        // Calculate memory allocation by component
        double procMemAllocated = transactions.stream().mapToDouble(Transaction::getProcMem).sum();
        double funcMemAllocated = transactions.stream().mapToDouble(Transaction::getFuncMem).sum();
        double dbMemAllocated = transactions.stream().mapToDouble(Transaction::getDbMem).sum();
        double streamMemAllocated = transactions.stream().mapToDouble(Transaction::getStreamMem).sum();
        
        stats.setProcMemAllocated(procMemAllocated);
        stats.setFuncMemAllocated(funcMemAllocated);
        stats.setDbMemAllocated(dbMemAllocated);
        stats.setStreamMemAllocated(streamMemAllocated);
        
        // Calculate memory allocation percentages
        if (totalMemoryAllocated > 0) {
            stats.setProcMemPercentage(procMemAllocated / totalMemoryAllocated * 100);
            stats.setFuncMemPercentage(funcMemAllocated / totalMemoryAllocated * 100);
            stats.setDbMemPercentage(dbMemAllocated / totalMemoryAllocated * 100);
            stats.setStreamMemPercentage(streamMemAllocated / totalMemoryAllocated * 100);
        }
        
        // Calculate memory allocation rate
        double totalTransactionTime = transactions.stream().mapToDouble(Transaction::getLength).sum();
        if (totalTransactionTime > 0) {
            stats.setMemoryAllocationRate(totalMemoryAllocated / totalTransactionTime);
        }
        
        // Calculate memory allocation per transaction
        stats.setMemoryPerTransaction(totalMemoryAllocated / transactions.size());
        
        return stats;
    }
    
    /**
     * Calculates a risk score for a potential memory leak.
     *
     * @param memoryEfficiency the memory efficiency (bytes per ms)
     * @param memoryRetention the memory retention ratio
     * @param memoryGrowthRate the memory growth rate
     * @return the calculated risk score
     */
    private double calculateMemoryLeakRiskScore(double memoryEfficiency, double memoryRetention, double memoryGrowthRate) {
        // Normalize each factor to a 0-1 scale
        double efficiencyScore = Math.min(memoryEfficiency / 10000, 1.0);
        double retentionScore = memoryRetention;
        double growthScore = Math.min(memoryGrowthRate / 100000, 1.0);
        
        // Calculate weighted average
        return (efficiencyScore * 0.3) + (retentionScore * 0.4) + (growthScore * 0.3);
    }
    
    /**
     * Inner class to hold memory statistics.
     */
    public static class MemoryStats {
        private int count;
        private double meanTotalMemory;
        private double minTotalMemory;
        private double maxTotalMemory;
        private double meanProcMem;
        private double minProcMem;
        private double maxProcMem;
        private double meanFuncMem;
        private double minFuncMem;
        private double maxFuncMem;
        private double meanDbMem;
        private double minDbMem;
        private double maxDbMem;
        private double meanStreamMem;
        private double minStreamMem;
        private double maxStreamMem;
        private double meanOsVmSize;
        private double minOsVmSize;
        private double maxOsVmSize;
        private double meanFreeMemory;
        private double minFreeMemory;
        private double maxFreeMemory;
        private double meanLength;
        private double minLength;
        private double maxLength;
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public double getMeanTotalMemory() {
            return meanTotalMemory;
        }
        
        public void setMeanTotalMemory(double meanTotalMemory) {
            this.meanTotalMemory = meanTotalMemory;
        }
        
        public double getMinTotalMemory() {
            return minTotalMemory;
        }
        
        public void setMinTotalMemory(double minTotalMemory) {
            this.minTotalMemory = minTotalMemory;
        }
        
        public double getMaxTotalMemory() {
            return maxTotalMemory;
        }
        
        public void setMaxTotalMemory(double maxTotalMemory) {
            this.maxTotalMemory = maxTotalMemory;
        }
        
        public double getMeanProcMem() {
            return meanProcMem;
        }
        
        public void setMeanProcMem(double meanProcMem) {
            this.meanProcMem = meanProcMem;
        }
        
        public double getMinProcMem() {
            return minProcMem;
        }
        
        public void setMinProcMem(double minProcMem) {
            this.minProcMem = minProcMem;
        }
        
        public double getMaxProcMem() {
            return maxProcMem;
        }
        
        public void setMaxProcMem(double maxProcMem) {
            this.maxProcMem = maxProcMem;
        }
        
        public double getMeanFuncMem() {
            return meanFuncMem;
        }
        
        public void setMeanFuncMem(double meanFuncMem) {
            this.meanFuncMem = meanFuncMem;
        }
        
        public double getMinFuncMem() {
            return minFuncMem;
        }
        
        public void setMinFuncMem(double minFuncMem) {
            this.minFuncMem = minFuncMem;
        }
        
        public double getMaxFuncMem() {
            return maxFuncMem;
        }
        
        public void setMaxFuncMem(double maxFuncMem) {
            this.maxFuncMem = maxFuncMem;
        }
        
        public double getMeanDbMem() {
            return meanDbMem;
        }
        
        public void setMeanDbMem(double meanDbMem) {
            this.meanDbMem = meanDbMem;
        }
        
        public double getMinDbMem() {
            return minDbMem;
        }
        
        public void setMinDbMem(double minDbMem) {
            this.minDbMem = minDbMem;
        }
        
        public double getMaxDbMem() {
            return maxDbMem;
        }
        
        public void setMaxDbMem(double maxDbMem) {
            this.maxDbMem = maxDbMem;
        }
        
        public double getMeanStreamMem() {
            return meanStreamMem;
        }
        
        public void setMeanStreamMem(double meanStreamMem) {
            this.meanStreamMem = meanStreamMem;
        }
        
        public double getMinStreamMem() {
            return minStreamMem;
        }
        
        public void setMinStreamMem(double minStreamMem) {
            this.minStreamMem = minStreamMem;
        }
        
        public double getMaxStreamMem() {
            return maxStreamMem;
        }
        
        public void setMaxStreamMem(double maxStreamMem) {
            this.maxStreamMem = maxStreamMem;
        }
        
        public double getMeanOsVmSize() {
            return meanOsVmSize;
        }
        
        public void setMeanOsVmSize(double meanOsVmSize) {
            this.meanOsVmSize = meanOsVmSize;
        }
        
        public double getMinOsVmSize() {
            return minOsVmSize;
        }
        
        public void setMinOsVmSize(double minOsVmSize) {
            this.minOsVmSize = minOsVmSize;
        }
        
        public double getMaxOsVmSize() {
            return maxOsVmSize;
        }
        
        public void setMaxOsVmSize(double maxOsVmSize) {
            this.maxOsVmSize = maxOsVmSize;
        }
        
        public double getMeanFreeMemory() {
            return meanFreeMemory;
        }
        
        public void setMeanFreeMemory(double meanFreeMemory) {
            this.meanFreeMemory = meanFreeMemory;
        }
        
        public double getMinFreeMemory() {
            return minFreeMemory;
        }
        
        public void setMinFreeMemory(double minFreeMemory) {
            this.minFreeMemory = minFreeMemory;
        }
        
        public double getMaxFreeMemory() {
            return maxFreeMemory;
        }
        
        public void setMaxFreeMemory(double maxFreeMemory) {
            this.maxFreeMemory = maxFreeMemory;
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
    }
    
    /**
     * Inner class to hold memory allocation statistics.
     */
    public static class MemoryAllocationStats {
        private int transactionCount;
        private double totalMemoryAllocated;
        private double procMemAllocated;
        private double funcMemAllocated;
        private double dbMemAllocated;
        private double streamMemAllocated;
        private double procMemPercentage;
        private double funcMemPercentage;
        private double dbMemPercentage;
        private double streamMemPercentage;
        private double memoryAllocationRate;
        private double memoryPerTransaction;
        
        public int getTransactionCount() {
            return transactionCount;
        }
        
        public void setTransactionCount(int transactionCount) {
            this.transactionCount = transactionCount;
        }
        
        public double getTotalMemoryAllocated() {
            return totalMemoryAllocated;
        }
        
        public void setTotalMemoryAllocated(double totalMemoryAllocated) {
            this.totalMemoryAllocated = totalMemoryAllocated;
        }
        
        public double getProcMemAllocated() {
            return procMemAllocated;
        }
        
        public void setProcMemAllocated(double procMemAllocated) {
            this.procMemAllocated = procMemAllocated;
        }
        
        public double getFuncMemAllocated() {
            return funcMemAllocated;
        }
        
        public void setFuncMemAllocated(double funcMemAllocated) {
            this.funcMemAllocated = funcMemAllocated;
        }
        
        public double getDbMemAllocated() {
            return dbMemAllocated;
        }
        
        public void setDbMemAllocated(double dbMemAllocated) {
            this.dbMemAllocated = dbMemAllocated;
        }
        
        public double getStreamMemAllocated() {
            return streamMemAllocated;
        }
        
        public void setStreamMemAllocated(double streamMemAllocated) {
            this.streamMemAllocated = streamMemAllocated;
        }
        
        public double getProcMemPercentage() {
            return procMemPercentage;
        }
        
        public void setProcMemPercentage(double procMemPercentage) {
            this.procMemPercentage = procMemPercentage;
        }
        
        public double getFuncMemPercentage() {
            return funcMemPercentage;
        }
        
        public void setFuncMemPercentage(double funcMemPercentage) {
            this.funcMemPercentage = funcMemPercentage;
        }
        
        public double getDbMemPercentage() {
            return dbMemPercentage;
        }
        
        public void setDbMemPercentage(double dbMemPercentage) {
            this.dbMemPercentage = dbMemPercentage;
        }
        
        public double getStreamMemPercentage() {
            return streamMemPercentage;
        }
        
        public void setStreamMemPercentage(double streamMemPercentage) {
            this.streamMemPercentage = streamMemPercentage;
        }
        
        public double getMemoryAllocationRate() {
            return memoryAllocationRate;
        }
        
        public void setMemoryAllocationRate(double memoryAllocationRate) {
            this.memoryAllocationRate = memoryAllocationRate;
        }
        
        public double getMemoryPerTransaction() {
            return memoryPerTransaction;
        }
        
        public void setMemoryPerTransaction(double memoryPerTransaction) {
            this.memoryPerTransaction = memoryPerTransaction;
        }
    }
    
    /**
     * Inner class to represent a potential memory leak source.
     */
    public static class MemoryLeakSource {
        private String actionName;
        private int transactionCount;
        private double meanTotalMemory;
        private double memoryEfficiency;
        private double memoryRetention;
        private double memoryGrowthRate;
        private double riskScore;
        
        public String getActionName() {
            return actionName;
        }
        
        public void setActionName(String actionName) {
            this.actionName = actionName;
        }
        
        public int getTransactionCount() {
            return transactionCount;
        }
        
        public void setTransactionCount(int transactionCount) {
            this.transactionCount = transactionCount;
        }
        
        public double getMeanTotalMemory() {
            return meanTotalMemory;
        }
        
        public void setMeanTotalMemory(double meanTotalMemory) {
            this.meanTotalMemory = meanTotalMemory;
        }
        
        public double getMemoryEfficiency() {
            return memoryEfficiency;
        }
        
        public void setMemoryEfficiency(double memoryEfficiency) {
            this.memoryEfficiency = memoryEfficiency;
        }
        
        public double getMemoryRetention() {
            return memoryRetention;
        }
        
        public void setMemoryRetention(double memoryRetention) {
            this.memoryRetention = memoryRetention;
        }
        
        public double getMemoryGrowthRate() {
            return memoryGrowthRate;
        }
        
        public void setMemoryGrowthRate(double memoryGrowthRate) {
            this.memoryGrowthRate = memoryGrowthRate;
        }
        
        public double getRiskScore() {
            return riskScore;
        }
        
        public void setRiskScore(double riskScore) {
            this.riskScore = riskScore;
        }
        
        /**
         * Gets the risk level based on the risk score.
         *
         * @return the risk level (High, Medium, or Low)
         */
        public String getRiskLevel() {
            if (riskScore >= 0.7) {
                return "High";
            } else if (riskScore >= 0.4) {
                return "Medium";
            } else {
                return "Low";
            }
        }
    }
}
