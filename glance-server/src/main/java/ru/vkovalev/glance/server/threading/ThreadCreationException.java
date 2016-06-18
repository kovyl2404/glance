package ru.vkovalev.glance.server.threading;

/**
 * Created by Viacheslav V. Kovalev <kovyl2404@gmail.com>
 * 18.06.16
 */
public class ThreadCreationException extends Exception {
    public ThreadCreationException(Exception cause) {
        super(cause);
    }
}
