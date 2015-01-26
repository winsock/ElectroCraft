package me.querol.electrocraft.core.computer;

import java.util.concurrent.*;

/**
 * Created by winsock on 1/25/15.
 */
public class ComputerThreadHandler {
    private static ComputerThreadHandler ourInstance = new ComputerThreadHandler();
    private ExecutorService executorService;

    public static ComputerThreadHandler getInstance() {
        return ourInstance;
    }

    private ComputerThreadHandler() {
        executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }

    public synchronized void enqueue(FutureTask<?> futureTask) {
        executorService.execute(futureTask);
    }

    public synchronized void enqueue(Runnable runnable) {
        executorService.execute(runnable);
    }
}
