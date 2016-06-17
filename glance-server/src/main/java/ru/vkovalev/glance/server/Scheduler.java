package ru.vkovalev.glance.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vkovalev on 17.06.16.
 */
public abstract class Scheduler <What, Worker> {

    protected Scheduler(Worker[] workers) {
        this.workers = workers;
        this.scheduled = new HashMap<>();
    }

    public Worker schedule(What what) {
        Integer scheduledIndex = scheduled.get(what);
        if ( scheduledIndex != null ) {
            return workers[scheduledIndex];
        }
        scheduledIndex = scheduleNew(what);
        scheduled.put(what, scheduledIndex);
        return workers[scheduledIndex];
    }

    public void unschedule(What what) {
        Integer workerIndex = scheduled.get(what);
        if (workerIndex == null) {
            return;
        }
        unscheduleExisting(workerIndex);
    }

    protected abstract int scheduleNew(What what);
    protected abstract void unscheduleExisting(int workerIndex);

    private final Worker[] workers;
    private final Map<What, Integer> scheduled;
}
