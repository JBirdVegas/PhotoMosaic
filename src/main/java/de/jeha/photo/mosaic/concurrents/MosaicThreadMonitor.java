package de.jeha.photo.mosaic.concurrents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class MosaicThreadMonitor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(MosaicThreadMonitor.class);

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
            LOG.info("[monitor] [{}/{}] active: {}, completed: {}, task: {}, isShutdown: {}, isTerminated: {}",
                    executor.getPoolSize(),
                    executor.getCorePoolSize(),
                    executor.getActiveCount(),
                    executor.getCompletedTaskCount(),
                    executor.getTaskCount(),
                    executor.isShutdown(),
                    executor.isTerminated());
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(delaySeconds));
            } catch (InterruptedException e) {
                LOG.warn("Unexpected interrupt", e);
            }
        }
    }

    void shutdown() {
        isRunning = false;
    }

}