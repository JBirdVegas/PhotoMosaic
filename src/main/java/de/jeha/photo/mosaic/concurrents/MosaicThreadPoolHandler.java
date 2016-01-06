package de.jeha.photo.mosaic.concurrents;

import java.util.concurrent.*;

public class MosaicThreadPoolHandler {
    private static ThreadPoolExecutor executor;
    private MosaicThreadMonitor mosaicThreadMonitor;

    public MosaicThreadPoolHandler(int numberOfThreads) {
        ThreadFactory factory = Executors.defaultThreadFactory();
        executor = new ThreadPoolExecutor(numberOfThreads, numberOfThreads * 2, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(100000), factory,
                (r, executor1) ->
                        System.err.println("Rejected runnable: " + r.toString()));
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
                System.err.println("Sleep was interrupted");
                e.printStackTrace();
            }
        }
        shutdown();
    }
}
