package ru.vkovalev.glance.server.threading;

/**
 * Created by Viacheslav V. Kovalev <kovyl2404@gmail.com>
 * 18.06.16
 */
public interface WorkersThreadFactory {
    AutoCloseableThread create(String threadName) throws ThreadCreationException ;
}
