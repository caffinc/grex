package com.caffinc.grex.common.entities;

/**
 * Describes one load point
 *
 * @author Sriram
 */
public class LoadEntity {
    private long timestamp;
    private double systemLoad;
    private double processLoad;
    private double processLoadApplied;

    public LoadEntity() {
    }

    public LoadEntity(long timestamp, double systemLoad, double processLoad, double processLoadApplied) {
        this.timestamp = timestamp;
        this.systemLoad = systemLoad;
        this.processLoad = processLoad;
        this.processLoadApplied = processLoadApplied;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getSystemLoad() {
        return systemLoad;
    }

    public void setSystemLoad(double systemLoad) {
        this.systemLoad = systemLoad;
    }

    public double getProcessLoad() {
        return processLoad;
    }

    public void setProcessLoad(double processLoad) {
        this.processLoad = processLoad;
    }

    public double getProcessLoadApplied() {
        return processLoadApplied;
    }

    public void setProcessLoadApplied(double processLoadApplied) {
        this.processLoadApplied = processLoadApplied;
    }
}
