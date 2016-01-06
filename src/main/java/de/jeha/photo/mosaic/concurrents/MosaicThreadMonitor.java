package de.jeha.photo.mosaic.concurrents;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class MosaicThreadMonitor implements Runnable {
    private ThreadPoolExecutor executor;
    private int seconds;
    private boolean isRunning = true;

    /* package */ MosaicThreadMonitor(ThreadPoolExecutor threadPoolExecutor, int delay) {
        executor = threadPoolExecutor;
        seconds = delay;
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
                Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void shutdown() {
        isRunning = false;
    }
}