package com.caffinc.grex.core;

import com.caffinc.grex.common.entities.LoadEntity;
import com.caffinc.grex.common.entities.NodeStatus;
import com.google.common.util.concurrent.AtomicDouble;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Generates Load on the CPU by keeping it busy for the given load percentage
 *
 * @author Sriram
 */
public class Load {
    private static final Logger LOG = LoggerFactory.getLogger(Load.class);
    public static final OperatingSystemMXBean OPERATING_SYSTEM_MX_BEAN = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final Load INSTANCE = new Load();

    private String id = UUID.randomUUID().toString();
    private AtomicBoolean running = new AtomicBoolean(false);
    private int numThreads = Runtime.getRuntime().availableProcessors();
    private AtomicDouble load = new AtomicDouble(0.5);
    private ControlThread controlThread = null;

    public static Load getInstance() {
        return INSTANCE;
    }

    public String getId() {
        return id;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public void setLoad(double load) {
        if (isRunning()) {
            LOG.info("Running load adjusted to {}", load);
            controlThread.setExpectedLoad(load);
        }
        this.load.set(load);
    }

    public void start() {
        LOG.info("Attempting to start node");
        if (!isRunning()) {
            running.set(true);
            for (int threadId = 0; threadId < numThreads; threadId++) {
                new BusyThread("BusyThread" + threadId, load, running).start();
            }
            controlThread = new ControlThread(load, running);
            controlThread.start();
            LOG.info("Node started");
        } else {
            LOG.info("Node already running");
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public void stop() throws InterruptedException {
        LOG.info("Attempting to stop node");
        if (isRunning()) {
            running.set(false);
            Thread.sleep(100);
            this.load.set(controlThread.getExpectedLoad());
            LOG.info("Node stopped");
        } else {
            LOG.info("Node already stopped");
        }
    }

    public NodeStatus getStatus() {
        NodeStatus status = new NodeStatus();
        status.setId(id);
        status.setRunning(isRunning());
        if (isRunning()) {
            status.setExpectedLoad(controlThread.getExpectedLoad());
        } else {
            status.setExpectedLoad(load.get());
        }
        status.setLoad(new LoadEntity(System.currentTimeMillis(), OPERATING_SYSTEM_MX_BEAN.getSystemCpuLoad(), OPERATING_SYSTEM_MX_BEAN.getProcessCpuLoad(), load.get()));
        return status;
    }

}
