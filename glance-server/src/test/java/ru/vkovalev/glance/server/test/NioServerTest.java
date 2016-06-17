package ru.vkovalev.glance.server.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.vkovalev.glance.server.ChannelCallback;
import ru.vkovalev.glance.server.io.NioServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by Viacheslav V. Kovalev <kovyl2404@gmail.com>
 * 18.06.16
 */
public class NioServerTest {

    public NioServerTest() throws IOException {
        connectedClients = new HashSet<>();
        nioServer = new NioServer(listenPort);
        nioServer.setChannelCallbackFactory( () -> new MockChannelCallback() );
    }

    @Test
    public void acceptAndCloseConnection() throws IOException {

        final Socket clientSocket = new Socket();
        try {
            clientSocket.connect(new InetSocketAddress(listenPort));
            syncWith(connectedClients);
            assertEquals(1, connectedClients.size());
        }
        finally {
            clientSocket.close();
        }
        syncWith(connectedClients);
        assertEquals(0, connectedClients.size());
    }

    @Before
    public void startServer() throws IOException {
        nioServer.start();
    }

    @After
    public void stopServer() throws IOException {
        nioServer.close();
    }

    private class MockChannelCallback implements ChannelCallback<SocketChannel> {

        @Override
        public void channelOpen(SocketChannel socketChannel) {
            connectedClients.add(socketChannel);
            notifyFor(connectedClients);
        }

        @Override
        public void channelClose(SocketChannel socketChannel) {
            try {
                socketChannel.close();
            }
            catch (IOException e) {

            }
            connectedClients.remove(socketChannel);
            notifyFor(connectedClients);
        }
    }

    private static void syncWith(Object lock) {
        synchronized (lock) {
            try {
                lock.wait(1000);
            }
            catch (InterruptedException e) {

            }
        }
    }

    private static void notifyFor(Object lock) {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private final Set<SocketChannel> connectedClients;
    private final NioServer nioServer;

    private static final int listenPort = 2345;
}
