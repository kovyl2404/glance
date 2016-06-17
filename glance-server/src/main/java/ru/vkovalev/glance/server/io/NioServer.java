package ru.vkovalev.glance.server.io;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.vkovalev.glance.server.ChannelCallback;
import ru.vkovalev.glance.server.ChannelCallbackFactory;
import ru.vkovalev.glance.server.UniformScheduler;
import ru.vkovalev.glance.server.Scheduler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * Created by vkovalev on 17.06.16.
 */
public class NioServer extends Thread implements AutoCloseable{

    public NioServer(int listenPort) throws IOException {
        super.setName("NioServer acceptor thread");
        this.listenPort = listenPort;
        workersThreadPool = new WorkerThreadPool(WORKERS_COUNT);
        scheduler = new UniformScheduler<>(this.workersThreadPool.getWorkers());
        logger = LogManager.getLogger(this.getClass());
        channelCallbackFactory = () -> new SocketChannelCallback();
    }

    public void setChannelCallbackFactory(ChannelCallbackFactory<SocketChannel> factory) {
        channelCallbackFactory = factory;
    }

    @Override
    public void start() {
        super.start();
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {

            }
        }
    }

    @Override
    public void run() {

        logger.info("NioServer started accepting connections.");
        try (final ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.bind(new InetSocketAddress(listenPort), BACKLOG_SIZE);
            synchronized (this) {
                this.notifyAll();
            }
            while ( ! isInterrupted() ) {
                final SocketChannel clientChannel = serverChannel.accept();
                final WorkerThread scheduledWorker = scheduler.schedule(clientChannel);
                scheduledWorker.register(clientChannel, channelCallbackFactory.createCallback());
            }
        }
        catch (IOException e) {
            logger.error("An error exception raised while accepting connections", e);
        }
        logger.info("NioServer stopped");

    }

    @Override
    public void close() throws IOException {
        workersThreadPool.close();
    }

    private static class SocketChannelCallback implements ChannelCallback<SocketChannel> {

        private SocketChannelCallback() {
            logger = LogManager.getLogger(this.getClass());
        }

        @Override
        public void channelOpen(SocketChannel socketChannel) {
            try {
                logger.info( String.format( "Start serving new connection %s",
                                            socketChannel.getRemoteAddress()) );
            }
            catch (IOException e) {

            }
        }

        @Override
        public void channelClose(SocketChannel socketChannel) {
            try {
                logger.info(String.format("Connection %s closed",
                        socketChannel.getRemoteAddress()));
            }
            catch (IOException e) {

            }
        }

        private final Logger logger;
    }

    private final int listenPort;
    private final Scheduler<SocketChannel, WorkerThread> scheduler;
    private final WorkerThreadPool workersThreadPool;
    private final Logger logger;

    private ChannelCallbackFactory<SocketChannel> channelCallbackFactory;

    private static final int WORKERS_COUNT = 8;
    private static final int BACKLOG_SIZE = 4096;
}
