package com.qserver.loganalyzer.model;

import java.time.LocalDateTime;

/**
 * Represents a transaction from QServer transaction logs.
 * This class encapsulates all the data related to a single transaction.
 */
public class Transaction {
    private long id;
    private String kind;
    private String thread;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double waitTime;
    private double processingTime;
    private double beginTime;
    private double funcTime;
    private double dbTime;
    private double memoryCommitTime;
    private double streamTime;
    private double kernelTime;
    private double cleanupTime;
    private double endProcessTime;
    private String status;
    private String initiator;
    private String details;

    /**
     * Default constructor
     */
    public Transaction() {
    }

    /**
     * Constructor with essential fields
     * 
     * @param id Transaction ID
     * @param kind Type of transaction
     * @param thread Thread that processed the transaction
     * @param startTime Time when transaction started
     * @param endTime Time when transaction ended
     * @param waitTime Time spent waiting
     * @param processingTime Time spent processing
     */
    public Transaction(long id, String kind, String thread, LocalDateTime startTime, 
                      LocalDateTime endTime, double waitTime, double processingTime) {
        this.id = id;
        this.kind = kind;
        this.thread = thread;
        this.startTime = startTime;
        this.endTime = endTime;
        this.waitTime = waitTime;
        this.processingTime = processingTime;
    }

    /**
     * Gets the transaction ID
     * 
     * @return Transaction ID
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the transaction ID
     * 
     * @param id Transaction ID
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the transaction kind/type
     * 
     * @return Transaction kind
     */
    public String getKind() {
        return kind;
    }

    /**
     * Sets the transaction kind/type
     * 
     * @param kind Transaction kind
     */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * Gets the thread that processed the transaction
     * 
     * @return Thread name
     */
    public String getThread() {
        return thread;
    }

    /**
     * Sets the thread that processed the transaction
     * 
     * @param thread Thread name
     */
    public void setThread(String thread) {
        this.thread = thread;
    }

    /**
     * Gets the transaction start time
     * 
     * @return Start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the transaction start time
     * 
     * @param startTime Start time
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the transaction end time
     * 
     * @return End time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Sets the transaction end time
     * 
     * @param endTime End time
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the transaction wait time
     * 
     * @return Wait time in milliseconds
     */
    public double getWaitTime() {
        return waitTime;
    }

    /**
     * Sets the transaction wait time
     * 
     * @param waitTime Wait time in milliseconds
     */
    public void setWaitTime(double waitTime) {
        this.waitTime = waitTime;
    }

    /**
     * Gets the transaction processing time
     * 
     * @return Processing time in milliseconds
     */
    public double getProcessingTime() {
        return processingTime;
    }

    /**
     * Sets the transaction processing time
     * 
     * @param processingTime Processing time in milliseconds
     */
    public void setProcessingTime(double processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * Gets the transaction begin time component
     * 
     * @return Begin time in milliseconds
     */
    public double getBeginTime() {
        return beginTime;
    }

    /**
     * Sets the transaction begin time component
     * 
     * @param beginTime Begin time in milliseconds
     */
    public void setBeginTime(double beginTime) {
        this.beginTime = beginTime;
    }

    /**
     * Gets the transaction function time component
     * 
     * @return Function time in milliseconds
     */
    public double getFuncTime() {
        return funcTime;
    }

    /**
     * Sets the transaction function time component
     * 
     * @param funcTime Function time in milliseconds
     */
    public void setFuncTime(double funcTime) {
        this.funcTime = funcTime;
    }

    /**
     * Gets the transaction database time component
     * 
     * @return Database time in milliseconds
     */
    public double getDbTime() {
        return dbTime;
    }

    /**
     * Sets the transaction database time component
     * 
     * @param dbTime Database time in milliseconds
     */
    public void setDbTime(double dbTime) {
        this.dbTime = dbTime;
    }

    /**
     * Gets the transaction memory commit time component
     * 
     * @return Memory commit time in milliseconds
     */
    public double getMemoryCommitTime() {
        return memoryCommitTime;
    }

    /**
     * Sets the transaction memory commit time component
     * 
     * @param memoryCommitTime Memory commit time in milliseconds
     */
    public void setMemoryCommitTime(double memoryCommitTime) {
        this.memoryCommitTime = memoryCommitTime;
    }

    /**
     * Gets the transaction stream time component
     * 
     * @return Stream time in milliseconds
     */
    public double getStreamTime() {
        return streamTime;
    }

    /**
     * Sets the transaction stream time component
     * 
     * @param streamTime Stream time in milliseconds
     */
    public void setStreamTime(double streamTime) {
        this.streamTime = streamTime;
    }

    /**
     * Gets the transaction kernel time component
     * 
     * @return Kernel time in milliseconds
     */
    public double getKernelTime() {
        return kernelTime;
    }

    /**
     * Sets the transaction kernel time component
     * 
     * @param kernelTime Kernel time in milliseconds
     */
    public void setKernelTime(double kernelTime) {
        this.kernelTime = kernelTime;
    }

    /**
     * Gets the transaction cleanup time component
     * 
     * @return Cleanup time in milliseconds
     */
    public double getCleanupTime() {
        return cleanupTime;
    }

    /**
     * Sets the transaction cleanup time component
     * 
     * @param cleanupTime Cleanup time in milliseconds
     */
    public void setCleanupTime(double cleanupTime) {
        this.cleanupTime = cleanupTime;
    }

    /**
     * Gets the transaction end process time component
     * 
     * @return End process time in milliseconds
     */
    public double getEndProcessTime() {
        return endProcessTime;
    }

    /**
     * Sets the transaction end process time component
     * 
     * @param endProcessTime End process time in milliseconds
     */
    public void setEndProcessTime(double endProcessTime) {
        this.endProcessTime = endProcessTime;
    }

    /**
     * Gets the transaction status
     * 
     * @return Transaction status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the transaction status
     * 
     * @param status Transaction status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the transaction initiator
     * 
     * @return Transaction initiator
     */
    public String getInitiator() {
        return initiator;
    }

    /**
     * Sets the transaction initiator
     * 
     * @param initiator Transaction initiator
     */
    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    /**
     * Gets the transaction details
     * 
     * @return Transaction details
     */
    public String getDetails() {
        return details;
    }

    /**
     * Sets the transaction details
     * 
     * @param details Transaction details
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * Calculates the total transaction length
     * 
     * @return Total transaction length in milliseconds
     */
    public double getTotalLength() {
        return beginTime + processingTime + funcTime + dbTime + 
               memoryCommitTime + streamTime + kernelTime + 
               cleanupTime + endProcessTime;
    }

    /**
     * Calculates the wait-to-processing ratio
     * 
     * @return Wait-to-processing ratio, or 0 if processing time is 0
     */
    public double getWaitToProcessingRatio() {
        if (processingTime == 0) {
            return 0;
        }
        return waitTime / processingTime;
    }

    /**
     * Returns a string representation of the transaction
     * 
     * @return String representation
     */
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", kind='" + kind + '\'' +
                ", thread='" + thread + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", waitTime=" + waitTime +
                ", processingTime=" + processingTime +
                '}';
    }
}
