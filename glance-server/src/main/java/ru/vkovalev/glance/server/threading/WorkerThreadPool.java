package ru.vkovalev.glance.server.threading;


public class WorkerThreadPool implements AutoCloseable {
    public WorkerThreadPool(int workersCount, WorkersThreadFactory factory) throws ThreadCreationException {
        workers = new AutoCloseableThread[workersCount];
        for ( int i = 0; i<workersCount; i++ ) {
            workers[i] = factory.create(String.format("Worker thread %d", i));
        }
    }

    public void start() {
        for (int i = 0; i < workers.length; i++) {
            workers[i].start();
        }
    }

    public void join() {
        try {
            for (Thread worker : workers) {
                worker.join();
            }
        }
        catch (InterruptedException e) {

        }
    }

    @Override
    public void close() {
        for (final AutoCloseableThread w : workers) {
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

    private final AutoCloseableThread[] workers;
}