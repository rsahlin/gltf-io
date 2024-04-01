
package org.gltfio.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gltfio.lib.Logger;

/**
 * Singleton to keep track of thread and executor usage.
 *
 */
public class ThreadService {

    private static ThreadService threadService;

    private final ExecutorService executorService;
    public final int threadCount;

    private ThreadService() {
        int processors = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(processors);
        threadCount = processors;
        Logger.d(getClass(), "Created executorservice with " + processors + " threads");
    }

    public static synchronized ThreadService getInstance() {
        if (threadService == null) {
            threadService = new ThreadService();
        }
        return threadService;
    }

    public synchronized void execute(Runnable command) {
        executorService.execute(command);
    }

}
