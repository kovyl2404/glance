package ru.vkovalev.glance.server.threading;

/**
 * Created by Viacheslav V. Kovalev <kovyl2404@gmail.com>
 * 18.06.16
 */
public abstract class AutoCloseableThread extends Thread implements AutoCloseable {
    @Override
    public abstract void close();
}
