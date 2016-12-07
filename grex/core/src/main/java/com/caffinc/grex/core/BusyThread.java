package com.caffinc.grex.core;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread that actually generates the given load
 *
 * @author Sriram
 */
public class BusyThread extends Thread {
    private AtomicDouble load;
    private AtomicBoolean running;

    /**
     * Constructor which creates the thread
     *
     * @param name    Name of this thread
     * @param load    Load % that this thread should generate
     * @param running Controls untimely stops
     */
    public BusyThread(String name, AtomicDouble load, AtomicBoolean running) {
        super(name);
        this.load = load;
        this.running = running;
    }

    /**
     * Generates the load when run
     */
    @Override
    public void run() {
        try {
            long prevTime = System.currentTimeMillis();
            // Loop for the given duration
            while (running.get()) {
                // Every 100ms, sleep for the percentage of unladen time
                if (System.currentTimeMillis() - prevTime >= 100) {
                    prevTime = System.currentTimeMillis();
                    Thread.sleep((long) Math.floor((1 - Math.min(load.get(), 1.0)) * 100));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}