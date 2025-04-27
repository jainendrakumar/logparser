package com.qserver.loganalyzer.visualization;

import com.qserver.loganalyzer.analyzer.MemoryAnalyzer;
import com.qserver.loganalyzer.analyzer.PerformanceAnalyzer;
import com.qserver.loganalyzer.model.Transaction;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates charts and visualizations for transaction analysis.
 * This class provides methods for creating various charts to visualize performance and memory metrics.
 */
public class ChartGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ChartGenerator.class);
    
    private final List<Transaction> transactions;
    private final PerformanceAnalyzer performanceAnalyzer;
    private final MemoryAnalyzer memoryAnalyzer;
    
    /**
     * Constructs a new ChartGenerator with the given transactions.
     *
     * @param transactions the list of transactions to visualize
     */
    public ChartGenerator(List<Transaction> transactions) {
        this.transactions = new ArrayList<>(transactions);
        this.performanceAnalyzer = new PerformanceAnalyzer(transactions);
        this.memoryAnalyzer = new MemoryAnalyzer(transactions);
        logger.info("Initialized ChartGenerator with {} transactions", transactions.size());
    }
    
    /**
     * Generates all charts and saves them to the specified directory.
     *
     * @param outputDirectory the directory to save the charts to
     * @throws IOException if an I/O error occurs
     */
    public void generateCharts(String outputDirectory) throws IOException {
        logger.info("Generating charts in directory: {}", outputDirectory);
        
        // Create charts directory if it doesn't exist
        File chartsDir = new File(outputDirectory, "charts");
        if (!chartsDir.exists()) {
            if (!chartsDir.mkdirs()) {
                logger.error("Failed to create charts directory: {}", chartsDir.getAbsolutePath());
                throw new IOException("Failed to create charts directory: " + chartsDir.getAbsolutePath());
            }
        }
        
        // Generate transaction length distribution chart
        generateTransactionLengthDistributionChart(chartsDir);
        
        // Generate transaction waiting time distribution chart
        generateTransactionWaitingTimeDistributionChart(chartsDir);
        
        // Generate transaction time components chart
        generateTransactionTimeComponentsChart(chartsDir);
        
        // Generate transaction count by hour chart
        generateTransactionCountByHourChart(chartsDir);
        
        // Generate transaction length by kind chart
        generateTransactionLengthByKindChart(chartsDir);
        
        // Generate waiting time by kind chart
        generateWaitingTimeByKindChart(chartsDir);
        
        // Generate memory usage over time chart
        generateMemoryUsageOverTimeChart(chartsDir);
        
        // Generate thread activity over time chart
        generateThreadActivityOverTimeChart(chartsDir);
        
        // Generate transaction length variation chart
        generateTransactionLengthVariationChart(chartsDir);
        
        logger.info("Chart generation completed successfully");
    }
    
    /**
     * Generates a chart showing the distribution of transaction lengths.
     *
     * @param outputDirectory the directory to save the chart to
     * @throws IOException if an I/O error occurs
     */
    private void generateTransactionLengthDistributionChart(File outputDirectory) throws IOException {
        logger.info("Generating transaction length distribution chart");
        
        // Create dataset
        XYSeries series = new XYSeries("Transaction Length");
        
        // Get finished transactions
        List<Transaction> finishedTransactions = transactions.stream()
                .filter(t -> "Finished".equals(t.getStatus()))
                .collect(Collectors.toList());
        
        // Create histogram data
        Map<Integer, Integer> histogram = new HashMap<>();
        int binSize = 100; // 100ms bins
        
        for (Transaction transaction : finishedTransactions) {
            int bin = (int) (transaction.getLength() / binSize) * binSize;
            histogram.put(bin, histogram.getOrDefault(bin, 0) + 1);
        }
        
        // Add data to series
        for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
            series.add(entry.getKey(), entry.getValue());
        }
        
        // Create dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        
        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Transaction Length Distribution",
                "Length (ms)",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Customize chart
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesLinesVisible(0, true);
        plot.setRenderer(renderer);
        
        // Save chart
        File outputFile = new File(outputDirectory, "transaction_length_distribution.png");
        ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
        logger.info("Transaction length distribution chart saved to: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Generates a chart showing the distribution of transaction waiting times.
     *
     * @param outputDirectory the directory to save the chart to
     * @throws IOException if an I/O error occurs
     */
    private void generateTransactionWaitingTimeDistributionChart(File outputDirectory) throws IOException {
        logger.info("Generating transaction waiting time distribution chart");
        
        // Create dataset
        XYSeries series = new XYSeries("Transaction Waiting Time");
        
        // Get finished transactions
        List<Transaction> finishedTransactions = transactions.stream()
                .filter(t -> "Finished".equals(t.getStatus()))
                .collect(Collectors.toList());
        
        // Create histogram data
        Map<Integer, Integer> histogram = new HashMap<>();
        int binSize = 100; // 100ms bins
        
        for (Transaction transaction : finishedTransactions) {
            int bin = (int) (transaction.getWaitingTime() / binSize) * binSize;
            histogram.put(bin, histogram.getOrDefault(bin, 0) + 1);
        }
        
        // Add data to series
        for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
            series.add(entry.getKey(), entry.getValue());
        }
        
        // Create dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        
        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Transaction Waiting Time Distribution",
                "Waiting Time (ms)",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Customize chart
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesLinesVisible(0, true);
        plot.setRenderer(renderer);
        
        // Save chart
        File outputFile = new File(outputDirectory, "transaction_waiting_time_distribution.png");
        ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
        logger.info("Transaction waiting time distribution chart saved to: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Generates a chart showing the breakdown of transaction time components.
     *
     * @param outputDirectory the directory to save the chart to
     * @throws IOException if an I/O error occurs
     */
    private void generateTransactionTimeComponentsChart(File outputDirectory) throws IOException {
        logger.info("Generating transaction time components chart");
        
        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Get finished transactions
        List<Transaction> finishedTransactions = transactions.stream()
                .filter(t -> "Finished".equals(t.getStatus()))
                .collect(Collectors.toList());
        
        // Calculate average time components
        double avgWaitingTime = finishedTransactions.stream()
                .mapToDouble(Transaction::getWaitingTime)
                .average()
                .orElse(0);
        
        double avgProcTime = finishedTransactions.stream()
                .mapToDouble(Transaction::getProcTime)
                .average()
                .orElse(0);
        
        double avgFuncTime = finishedTransactions.stream()
                .mapToDouble(Transaction::getFuncTime)
                .average()
                .orElse(0);
        
        double avgDbTime = finishedTransactions.stream()
                .mapToDouble(Transaction::getDbTime)
                .average()
                .orElse(0);
        
        double avgStreamTime = finishedTransactions.stream()
                .mapToDouble(Transaction::getStreamTime)
                .average()
                .orElse(0);
        
        // Add data to dataset
        dataset.addValue(avgWaitingTime, "Average Time (ms)", "Waiting Time");
        dataset.addValue(avgProcTime, "Average Time (ms)", "Processing Time");
        dataset.addValue(avgFuncTime, "Average Time (ms)", "Functional Time");
        dataset.addValue(avgDbTime, "Average Time (ms)", "Database Time");
        dataset.addValue(avgStreamTime, "Average Time (ms)", "Stream Time");
        
        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Average Transaction Time Components",
                "Component",
                "Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Customize chart
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        
        // Save chart
        File outputFile = new File(outputDirectory, "transaction_time_components.png");
        ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
        logger.info("Transaction time components chart saved to: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Generates a chart showing the transaction count by hour of day.
     *
     * @param outputDirectory the directory to save the chart to
     * @throws IOException if an I/O error occurs
     */
    private void generateTransactionCountByHourChart(File outputDirectory) throws IOException {
        logger.info("Generating transaction count by hour chart");
        
        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Get transactions with start time
        List<Transaction> transactionsWithTime = transactions.stream()
                .filter(t -> t.getStartTime() != null)
                .collect(Collectors.toList());
        
        // Count transactions by hour
        Map<Integer, Integer> countByHour = new HashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            countByHour.put(hour, 0);
        }
        
        for (Transaction transaction : transactionsWithTime) {
            int hour = transaction.getStartTime().getHour();
            countByHour.put(hour, countByHour.getOrDefault(hour, 0) + 1);
        }
        
        // Add data to dataset
        for (int hour = 0; hour < 24; hour++) {
            dataset.addValue(countByHour.get(hour), "Transaction Count", String.format("%02d:00", hour));
        }
        
        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Transaction Count by Hour of Day",
                "Hour",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Customize chart
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.GREEN);
        
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.1);
        
        // Save chart
        File outputFile = new File(outputDirectory, "transaction_count_by_hour.png");
        ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
        logger.info("Transaction count by hour chart saved to: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Generates a chart showing the average transaction length by transaction kind.
     *
     * @param outputDirectory the directory to save the chart to
     * @throws IOException if an I/O error occurs
     */
    private void generateTransactionLengthByKindChart(File outputDirectory) throws IOException {
        logger.info("Generating transaction length by kind chart");
        
        // Get performance statistics by transaction kind
        Map<String, PerformanceAnalyzer.TransactionStats> kindStats = performanceAnalyzer.analyzeByTransactionKind();
        
        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Add data to dataset (top 10 by mean length)
        kindStats.entrySet().stream()
                .sorted(Map.Entry.<String, PerformanceAnalyzer.TransactionStats>comparingByValue(
                        Comparator.comparingDouble(PerformanceAnalyzer.TransactionStats::getMeanLength).reversed()))
                .limit(10)
                .forEach(entry -> {
                    String kind = entry.getKey();
                    PerformanceAnalyzer.TransactionStats stats = entry.getValue();
                    dataset.addValue(stats.getMeanLength(), "Average Length (ms)", kind);
                });
        
        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Average Transaction Length by Kind (Top 10)",
                "Transaction Kind",
                "Length (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Customize chart
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.1);
        domainAxis.setMaximumCategoryLabelLines(2);
        
        // Save chart
        File outputFile = new File(outputDirectory, "transaction_length_by_kind.png");
        ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
        logger.info("Transaction length by kind chart saved to: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Generates a chart showing the average waiting time by transaction kind.
     *
     * @param outputDirectory the directory to save the chart to
     * @throws IOException if an I/O error occurs
     */
    private void generateWaitingTimeByKindChart(File outputDirectory) throws IOException {
        logger.info("Generating waiting time by kind chart");
        
        // Get performance statistics by transaction kind
        Map<String, PerformanceAnalyzer.TransactionStats> kindStats = performanceAnalyzer.analyzeByTransactionKind();
        
        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Add data to dataset (top 10 by mean waiting time)
        kindStats.entrySet().stream()
                .sorted(Map.Entry.<String, PerformanceAnalyzer.TransactionStats>comparingByValue(
                        Comparator.comparingDouble(PerformanceAnalyzer.TransactionStats::getMeanWaitingTime).reversed()))
                .limit(10)
                .forEach(entry -> {
                    String kind = entry.getKey();
                    PerformanceAnalyzer.TransactionStats stats = entry.getValue();
                    dataset.addValue(stats.getMeanWaitingTime(), "Average Waiting Time (ms)", kind);
                });
        
        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Average Waiting Time by Kind (Top 10)",
                "Transaction Kind",
                "Waiting Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Customize chart
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.1);
        domainAxis.setMaximumCategoryLabelLines(2);
        
        // Save chart
        File outputFile = new File(outputDirectory, "waiting_time_by_kind.png");
        ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
        logger.info("Waiting time by kind chart saved to: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Generates a chart showing memory usage over time.
     *
     * @param outputDirectory the directory to save the chart to
     * @throws IOException if an I/O error occurs
     */
    private void generateMemoryUsageOverTimeChart(File outputDirectory) throws IOException {
        logger.info("Generating memory usage over time chart");
        
        // Get memory trends over time
        Map<String, MemoryAnalyzer.MemoryStats> memoryTrends = memoryAnalyzer.analyzeMemoryTrendsOverTime();
        
        // Create dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        
        // Create time series for OS VM size
        TimeSeries osVmSizeSeries = new TimeSeries("OS VM Size");
        
        // Create time series for free memory
        TimeSeries freeMemorySeries = new TimeSeries("Free Memory");
        
        // Add data to series
        for (Map.Entry<String, MemoryAnalyzer.MemoryStats> entry : memoryTrends.entrySet()) {
            String timestamp = entry.getKey();
            MemoryAnalyzer.MemoryStats stats = entry.getValue();
            
            // Parse timestamp
            String[] parts = timestamp.split(" ");
            String datePart = parts[0];
            String timePart = parts[1];
            
            String[] dateParts = datePart.split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int day = Integer.parseInt(dateParts[2]);
            
            String[] timeParts = timePart.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            
            // Create minute for time series
            Minute timePoint = new Minute(minute, hour, day, month, year);
            
            // Add data points
            osVmSizeSeries.add(timePoint, stats.getMeanOsVmSize() / (1024 * 1024)); // Convert to MB
            freeMemorySeries.add(timePoint, stats.getMeanFreeMemory() / (1024 * 1024)); // Convert to MB
        }
        
        // Add series to dataset
        dataset.addSeries(osVmSizeSeries);
        dataset.addSeries(freeMemorySeries);
        
        // Create chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Memory Usage Over Time",
                "Time",
                "Memory (MB)",
                dataset,
                true,
                true,
                false
        );
        
        // Customize chart
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.GREEN);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);
        
        // Save chart
        File outputFile = new File(outputDirectory, "memory_usage_over_time.png");
        ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
        logger.info("Memory usage over time chart saved to: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Generates a chart showing thread activity over time.
     *
     * @param outputDirectory the directory to save the chart to
     * @throws IOException if an I/O error occurs
     */
    private void generateThreadActivityOverTimeChart(File outputDirectory) throws IOException {
        logger.info("Generating thread activity over time chart");
        
        // Get transactions with start time
        List<Transaction> transactionsWithTime = transactions.stream()
                .filter(t -> t.getStartTime() != null)
                .sorted(Comparator.comparing(Transaction::getStartTime))
                .collect(Collectors.toList());
        
        if (transactionsWithTime.isEmpty()) {
            logger.warn("No transactions with start time found, skipping thread activity chart");
            return;
        }
        
        // Create dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        
        // Create time series for active threads
        TimeSeries activeThreadsSeries = new TimeSeries("Active Threads");
        
        // Calculate active threads over time
        Map<LocalDateTime, Integer> activeThreadsCount = new TreeMap<>();
        
        // Initialize with minute intervals
        LocalDateTime startTime = transactionsWithTime.get(0).getStartTime();
        LocalDateTime endTime = transactionsWithTime.get(transactionsWithTime.size() - 1).getStartTime();
        
        LocalDateTime current = startTime;
        while (!current.isAfter(endTime)) {
            activeThreadsCount.put(current, 0);
            current = current.plusMinutes(1);
        }
        
        // Count active transactions at each minute
        for (Transaction transaction : transactionsWithTime) {
            LocalDateTime txStartTime = transaction.getStartTime();
            LocalDateTime txEndTime = transaction.getEndTime();
            
            if (txEndTime == null) continue;
            
            // Round to minute
            LocalDateTime roundedStart = txStartTime.withSecond(0).withNano(0);
            LocalDateTime roundedEnd = txEndTime.withSecond(0).withNano(0);
            
            // Increment count for each minute the transaction was active
            LocalDateTime minute = roundedStart;
            while (!minute.isAfter(roundedEnd)) {
                activeThreadsCount.put(minute, activeThreadsCount.getOrDefault(minute, 0) + 1);
                minute = minute.plusMinutes(1);
            }
        }
        
        // Add data to series
        for (Map.Entry<LocalDateTime, Integer> entry : activeThreadsCount.entrySet()) {
            LocalDateTime dateTime = entry.getKey();
            int count = entry.getValue();
            
            // Convert to Minute for JFreeChart
            Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            
            Minute minute = new Minute(
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.YEAR)
            );
            
            activeThreadsSeries.add(minute, count);
        }
        
        // Add series to dataset
        dataset.addSeries(activeThreadsSeries);
        
        // Create chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Thread Activity Over Time",
                "Time",
                "Active Threads",
                dataset,
                true,
                true,
                false
        );
        
        // Customize chart
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(renderer);
        
        // Set integer y-axis
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        // Save chart
        File outputFile = new File(outputDirectory, "thread_activity_over_time.png");
        ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
        logger.info("Thread activity over time chart saved to: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Generates a chart showing transaction length variation for similar transactions.
     *
     * @param outputDirectory the directory to save the chart to
     * @throws IOException if an I/O error occurs
     */
    private void generateTransactionLengthVariationChart(File outputDirectory) throws IOException {
        logger.info("Generating transaction length variation chart");
        
        // Get performance statistics by transaction kind
        Map<String, PerformanceAnalyzer.TransactionStats> kindStats = performanceAnalyzer.analyzeByTransactionKind();
        
        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Add data to dataset (top 10 by coefficient of variation)
        kindStats.entrySet().stream()
                .filter(entry -> entry.getValue().getCount() >= 10) // Only consider kinds with enough samples
                .sorted(Map.Entry.<String, PerformanceAnalyzer.TransactionStats>comparingByValue(
                        Comparator.comparingDouble(stats -> 
                                stats.getMaxLength() / stats.getMinLength())))
                .limit(10)
                .forEach(entry -> {
                    String kind = entry.getKey();
                    PerformanceAnalyzer.TransactionStats stats = entry.getValue();
                    dataset.addValue(stats.getMinLength(), "Min Length", kind);
                    dataset.addValue(stats.getMeanLength(), "Mean Length", kind);
                    dataset.addValue(stats.getMaxLength(), "Max Length", kind);
                });
        
        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Transaction Length Variation by Kind (Top 10)",
                "Transaction Kind",
                "Length (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Customize chart
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.GREEN);
        renderer.setSeriesPaint(1, Color.BLUE);
        renderer.setSeriesPaint(2, Color.RED);
        
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.1);
        domainAxis.setMaximumCategoryLabelLines(2);
        
        // Save chart
        File outputFile = new File(outputDirectory, "transaction_length_variation.png");
        ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
        logger.info("Transaction length variation chart saved to: {}", outputFile.getAbsolutePath());
    }
}
