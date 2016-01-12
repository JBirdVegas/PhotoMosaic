package de.jeha.photo.mosaic.concurrents;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class MosaicThreadMonitor implements Runnable {

    private final ThreadPoolExecutor executor;
    private final int delaySeconds;
    private boolean isRunning = true;

    MosaicThreadMonitor(ThreadPoolExecutor executor, int delaySeconds) {
        this.executor = executor;
        this.delaySeconds = delaySeconds;
    }

    @Override
    public void run() {
        while (isRunning) {
            System.out.println(
                    String.format("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
                            executor.getPoolSize(),
                            executor.getCorePoolSize(),
                            executor.getActiveCount(),
                            executor.getCompletedTaskCount(),
                            executor.getTaskCount(),
                            executor.isShutdown(),
                            executor.isTerminated()));
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(delaySeconds));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void shutdown() {
        isRunning = false;
    }

}