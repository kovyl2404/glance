package ru.vkovalev.glance.server;

/**
 * Created by vkovalev on 17.06.16.
 */
public class UniformScheduler<What, Worker> extends Scheduler<What,Worker> {

    public UniformScheduler(Worker[] workers) {
        super(workers);

        workersLoad = new int[workers.length];
    }

    @Override
    protected int scheduleNew(What what) {
        int currentWorker = 0;
        int currentLoad = workersLoad[currentWorker];
        for (int i = 1; i < workersLoad.length; i++ ) {
            if ( currentLoad > workersLoad[i] ) {
                currentLoad = workersLoad[i];
                currentWorker = i;
            }
        }
        workersLoad[currentWorker]++;
        return currentWorker;
    }

    @Override
    protected void unscheduleExisting(int workerIndex) {
        workersLoad[workerIndex]--;
    }

    private final int[] workersLoad;
}
