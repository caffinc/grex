package com.caffinc.grex.core;

import com.google.common.util.concurrent.AtomicDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Controls the execution of the Load Generator and balances the load
 *
 * @author Sriram
 */
public class ControlThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(ControlThread.class);

    private static final int GRANULARITY = 1000;
    /*
    Adjusts the load created by the BusyThread objects once every ADJUST_PERIOD times the GRANULARITY
    i.e.
    if GRANULARITY is 1000 (ms) and ADJUST_PERIOD is 2, adjustments to the load occur once every 2 * 1000 (ms) = 2s
    */
    private static final int ADJUST_PERIOD = 1;

    private AtomicBoolean running;
    private AtomicDouble load;
    private double expectedLoad;

    public ControlThread(AtomicDouble load, AtomicBoolean running) {
        super("ControlThread");
        this.running = running;
        this.load = load;
        this.expectedLoad = load.get();
    }

    public void setExpectedLoad(double expectedLoad) {
        this.expectedLoad = expectedLoad;
        LOG.info("Expected load changed to {}", expectedLoad);
    }

    public double getExpectedLoad() {
        return expectedLoad;
    }

    @Override
    public void run() {
        AtomicLong counter = new AtomicLong(0);
        double totalLoad = 0;
        while (running.get()) {
            try {
                Thread.sleep(GRANULARITY);
                if (running.get()) {
                    counter.incrementAndGet();
                    double currentLoad = Load.OPERATING_SYSTEM_MX_BEAN.getSystemCpuLoad();
                    totalLoad += currentLoad;
                    if (counter.get() == ADJUST_PERIOD) {
                        counter.set(0);
                        double delta = totalLoad / ADJUST_PERIOD - expectedLoad;
                        // Adjust load if the average was far from the expected amount
                        if (Math.abs(delta) > 0.025) {
                            double newLoad = load.get() - (delta / 2);
                            if (newLoad >= 0 && newLoad <= 1) {
                                load.addAndGet(-delta / 2);
                            }
                        }
                        totalLoad = 0;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
