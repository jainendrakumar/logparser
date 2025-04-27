package com.qserver.loganalyzer.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a QServer transaction with all its performance metrics.
 * This class encapsulates all the data from a transaction log entry.
 */
public class Transaction {
    private String transactionId;
    private String transactionKind;
    private String status;
    private String threadName;
    private String actionElementName;
    private LocalDateTime startTime;
    private long length;
    private long waitingTime;
    private long procTime;
    private long funcTime;
    private long dbTime;
    private long streamTime;
    private long procMem;
    private long funcMem;
    private long dbMem;
    private long streamMem;
    private long osVmSize;
    private long freeMemory;
    private String sourceFile;

    /**
     * Default constructor
     */
    public Transaction() {
    }

    /**
     * Gets the transaction ID
     * @return the transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the transaction ID
     * @param transactionId the transaction ID to set
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Gets the transaction kind
     * @return the transaction kind
     */
    public String getTransactionKind() {
        return transactionKind;
    }

    /**
     * Sets the transaction kind
     * @param transactionKind the transaction kind to set
     */
    public void setTransactionKind(String transactionKind) {
        this.transactionKind = transactionKind;
    }

    /**
     * Gets the transaction status
     * @return the transaction status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the transaction status
     * @param status the transaction status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the thread name
     * @return the thread name
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * Sets the thread name
     * @param threadName the thread name to set
     */
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    /**
     * Gets the action element name
     * @return the action element name
     */
    public String getActionElementName() {
        return actionElementName;
    }

    /**
     * Sets the action element name
     * @param actionElementName the action element name to set
     */
    public void setActionElementName(String actionElementName) {
        this.actionElementName = actionElementName;
    }

    /**
     * Gets the start time
     * @return the start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time
     * @param startTime the start time to set
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the total transaction length in milliseconds
     * @return the transaction length
     */
    public long getLength() {
        return length;
    }

    /**
     * Sets the total transaction length in milliseconds
     * @param length the transaction length to set
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * Gets the waiting time in milliseconds
     * @return the waiting time
     */
    public long getWaitingTime() {
        return waitingTime;
    }

    /**
     * Sets the waiting time in milliseconds
     * @param waitingTime the waiting time to set
     */
    public void setWaitingTime(long waitingTime) {
        this.waitingTime = waitingTime;
    }

    /**
     * Gets the processing time in milliseconds
     * @return the processing time
     */
    public long getProcTime() {
        return procTime;
    }

    /**
     * Sets the processing time in milliseconds
     * @param procTime the processing time to set
     */
    public void setProcTime(long procTime) {
        this.procTime = procTime;
    }

    /**
     * Gets the functional time in milliseconds
     * @return the functional time
     */
    public long getFuncTime() {
        return funcTime;
    }

    /**
     * Sets the functional time in milliseconds
     * @param funcTime the functional time to set
     */
    public void setFuncTime(long funcTime) {
        this.funcTime = funcTime;
    }

    /**
     * Gets the database time in milliseconds
     * @return the database time
     */
    public long getDbTime() {
        return dbTime;
    }

    /**
     * Sets the database time in milliseconds
     * @param dbTime the database time to set
     */
    public void setDbTime(long dbTime) {
        this.dbTime = dbTime;
    }

    /**
     * Gets the stream time in milliseconds
     * @return the stream time
     */
    public long getStreamTime() {
        return streamTime;
    }

    /**
     * Sets the stream time in milliseconds
     * @param streamTime the stream time to set
     */
    public void setStreamTime(long streamTime) {
        this.streamTime = streamTime;
    }

    /**
     * Gets the processing memory in bytes
     * @return the processing memory
     */
    public long getProcMem() {
        return procMem;
    }

    /**
     * Sets the processing memory in bytes
     * @param procMem the processing memory to set
     */
    public void setProcMem(long procMem) {
        this.procMem = procMem;
    }

    /**
     * Gets the functional memory in bytes
     * @return the functional memory
     */
    public long getFuncMem() {
        return funcMem;
    }

    /**
     * Sets the functional memory in bytes
     * @param funcMem the functional memory to set
     */
    public void setFuncMem(long funcMem) {
        this.funcMem = funcMem;
    }

    /**
     * Gets the database memory in bytes
     * @return the database memory
     */
    public long getDbMem() {
        return dbMem;
    }

    /**
     * Sets the database memory in bytes
     * @param dbMem the database memory to set
     */
    public void setDbMem(long dbMem) {
        this.dbMem = dbMem;
    }

    /**
     * Gets the stream memory in bytes
     * @return the stream memory
     */
    public long getStreamMem() {
        return streamMem;
    }

    /**
     * Sets the stream memory in bytes
     * @param streamMem the stream memory to set
     */
    public void setStreamMem(long streamMem) {
        this.streamMem = streamMem;
    }

    /**
     * Gets the OS virtual memory size in bytes
     * @return the OS virtual memory size
     */
    public long getOsVmSize() {
        return osVmSize;
    }

    /**
     * Sets the OS virtual memory size in bytes
     * @param osVmSize the OS virtual memory size to set
     */
    public void setOsVmSize(long osVmSize) {
        this.osVmSize = osVmSize;
    }

    /**
     * Gets the free memory in bytes
     * @return the free memory
     */
    public long getFreeMemory() {
        return freeMemory;
    }

    /**
     * Sets the free memory in bytes
     * @param freeMemory the free memory to set
     */
    public void setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
    }

    /**
     * Gets the source file name
     * @return the source file name
     */
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * Sets the source file name
     * @param sourceFile the source file name to set
     */
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * Calculates the waiting time percentage relative to the total transaction length
     * @return the waiting time percentage
     */
    public double getWaitingTimePercentage() {
        return length > 0 ? (double) waitingTime / length * 100 : 0;
    }

    /**
     * Calculates the processing time percentage relative to the total transaction length
     * @return the processing time percentage
     */
    public double getProcTimePercentage() {
        return length > 0 ? (double) procTime / length * 100 : 0;
    }

    /**
     * Calculates the functional time percentage relative to the total transaction length
     * @return the functional time percentage
     */
    public double getFuncTimePercentage() {
        return length > 0 ? (double) funcTime / length * 100 : 0;
    }

    /**
     * Calculates the database time percentage relative to the total transaction length
     * @return the database time percentage
     */
    public double getDbTimePercentage() {
        return length > 0 ? (double) dbTime / length * 100 : 0;
    }

    /**
     * Calculates the stream time percentage relative to the total transaction length
     * @return the stream time percentage
     */
    public double getStreamTimePercentage() {
        return length > 0 ? (double) streamTime / length * 100 : 0;
    }

    /**
     * Determines the dominant time component (the component that takes the most time)
     * @return the name of the dominant time component
     */
    public String getDominantTimeComponent() {
        long max = Math.max(waitingTime, Math.max(procTime, Math.max(funcTime, Math.max(dbTime, streamTime))));
        
        if (max == waitingTime) return "waitingtime";
        if (max == procTime) return "proctime";
        if (max == funcTime) return "functime";
        if (max == dbTime) return "dbtime";
        if (max == streamTime) return "streamtime";
        
        return "unknown";
    }

    /**
     * Calculates the end time of the transaction
     * @return the end time
     */
    public LocalDateTime getEndTime() {
        if (startTime == null) return null;
        return startTime.plusNanos(length * 1_000_000); // Convert ms to ns
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", transactionKind='" + transactionKind + '\'' +
                ", status='" + status + '\'' +
                ", length=" + length +
                ", waitingTime=" + waitingTime +
                ", procTime=" + procTime +
                ", funcTime=" + funcTime +
                ", dbTime=" + dbTime +
                ", streamTime=" + streamTime +
                '}';
    }
}
