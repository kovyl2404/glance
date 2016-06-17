package ru.vkovalev.glance.server.io;

import java.io.IOException;

public class WorkerThreadPool implements AutoCloseable {
    public WorkerThreadPool(int workersCount) throws IOException {
        workers = new WorkerThread[workersCount];
        for ( int i = 0; i<workersCount; i++ ) {
            workers[i] = new WorkerThread(i);
            workers[i].start();
        }
    }

    public WorkerThread[] getWorkers() {
        return workers;
    }

    @Override
    public void close() throws IOException {
        for (final WorkerThread w : workers) {
            w.interrupt();
            try {
                w.join();
                w.close();
            }
            catch (InterruptedException e) {
                // We can not interrupt here because all workers must be terminated before.
                // Just ignore this exception and continue
            }
        }
    }

    private final WorkerThread[] workers;
}