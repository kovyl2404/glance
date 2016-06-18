package ru.vkovalev.glance.server.io;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.vkovalev.glance.server.ChannelCallback;
import ru.vkovalev.glance.server.ChannelCallbackFactory;
import ru.vkovalev.glance.server.threading.AutoCloseableThread;
import ru.vkovalev.glance.server.threading.ThreadCreationException;
import ru.vkovalev.glance.server.threading.WorkerThreadPool;
import ru.vkovalev.glance.server.threading.WorkersThreadFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by vkovalev on 17.06.16.
 */
public class NioServer implements AutoCloseable{

    public NioServer( int listenPort )
            throws IOException, ThreadCreationException {

        this( listenPort,  () -> new MyChannelCallback() );

    }

    public NioServer( int listenPort, ChannelCallbackFactory channelCallbackFactory )
            throws IOException, ThreadCreationException {

        final ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(listenPort), BACKLOG_SIZE);

        final WorkersThreadFactory threadFactory =
                (name) -> new NioWorkerThread(name, server, channelCallbackFactory);
        workersThreadPool =
                new WorkerThreadPool( WORKERS_COUNT, threadFactory);

        logger = LogManager.getLogger(this.getClass());

    }


    @Override
    public void close() throws IOException {
        workersThreadPool.close();
    }

    public void start() {
        workersThreadPool.start();
    }

    public void join() {
        workersThreadPool.join();
    }


    private static class MyChannelCallback implements ChannelCallback {

        @Override
        public void channelOpen() {

        }

        @Override
        public void channelClose() {

        }

        @Override
        public void channelData(ByteBuffer data) {

        }

    }


    private final WorkerThreadPool workersThreadPool;
    private final Logger logger;

    private static final int WORKERS_COUNT = 8;
    private static final int BACKLOG_SIZE = 4096;
}
