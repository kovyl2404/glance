package ru.vkovalev.glance.server.test;

import org.junit.Test;
import ru.vkovalev.glance.server.Scheduler;
import ru.vkovalev.glance.server.UniformScheduler;

import static org.junit.Assert.assertEquals;

/**
 * Created by Viacheslav V. Kovalev <kovyl2404@gmail.com>
 * 18.06.16
 */
public class UniformSchedulerTest {

    public UniformSchedulerTest() {

        scheduler = new UniformScheduler<>(workers);
    }

    @Test
    public void scheduleSameWork() {
        final String firstAttemt = scheduler.schedule("foo");
        final String secondAttempt = scheduler.schedule("foo");
        assertEquals(firstAttemt, secondAttempt);
    }

    @Test
    public void  uniformLoad() {
        for ( int i = 0; i < workers.length; i++ ) {
            final String work = Integer.toString(i);
            final String scheduledWorker = scheduler.schedule(work);
            assertEquals(Integer.toString(i), scheduledWorker);
        }
    }

    @Test
    public void unscheduleExistent() {
        final String fooWorker = scheduler.schedule("foo");
        scheduler.unschedule("foo");
        final String barWorker = scheduler.schedule("bar");
        assertEquals("0", fooWorker);
        assertEquals("0", barWorker);
    }

    @Test
    public void unscheduleNonExistent() {
        scheduler.unschedule("foo");
            // Just check that all ok
    }

    private static final String[] workers = { "0", "1", "2", "3" };
    private final Scheduler<String, String> scheduler;
}
