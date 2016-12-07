package com.caffinc.grex.common.entities;

/**
 * Holds the status of the system
 *
 * @author Sriram
 */
public class NodeStatus {
    private String id;
    private boolean running;
    private double expectedLoad;
    private LoadEntity load;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public double getExpectedLoad() {
        return expectedLoad;
    }

    public void setExpectedLoad(double expectedLoad) {
        this.expectedLoad = expectedLoad;
    }

    public LoadEntity getLoad() {
        return load;
    }

    public void setLoad(LoadEntity load) {
        this.load = load;
    }
}
