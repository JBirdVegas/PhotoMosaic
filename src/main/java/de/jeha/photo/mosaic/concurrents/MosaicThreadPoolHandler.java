package de.jeha.photo.mosaic.concurrents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class MosaicThreadPoolHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MosaicThreadPoolHandler.class);

    private final ThreadPoolExecutor executor;
    private final MosaicThreadMonitor mosaicThreadMonitor;

    public MosaicThreadPoolHandler(int numberOfThreads) {
        ThreadFactory factory = Executors.defaultThreadFactory();
        executor = new ThreadPoolExecutor(
                numberOfThreads,
                numberOfThreads * 2,
                10,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100_000), factory, (r, e) -> LOG.error("Rejected runnable {}", r.toString())
        );
        mosaicThreadMonitor = new MosaicThreadMonitor(executor, 5);
        Thread thread = new Thread(mosaicThreadMonitor);
        thread.start();
    }

    public void addJob(Runnable runnable) {
        executor.execute(runnable);
    }

    public void shutdown() {
        mosaicThreadMonitor.shutdown();
        executor.shutdown();
    }

    public void shutdownWhenDone() {
        while (executor.getActiveCount() > 0) {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                LOG.warn("Unexpected interrupt", e);
            }
        }
        shutdown();
    }

}
