package ru.vkovalev.glance.server.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.vkovalev.glance.server.ChannelCallback;
import ru.vkovalev.glance.server.io.NioServer;
import ru.vkovalev.glance.server.threading.ThreadCreationException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by Viacheslav V. Kovalev <kovyl2404@gmail.com>
 * 18.06.16
 */
public class NioServerTest {

    public NioServerTest() throws IOException, ThreadCreationException {
        counter = 0;
        nioServer = new NioServer(listenPort, () -> new MockChannelCallback());
    }

    @Test
    public void acceptAndCloseConnection() throws IOException, InterruptedException {

        try (final Socket clientSocket = new Socket()) {
            clientSocket.connect(new InetSocketAddress(listenPort));
            synchronized (counter) {
                counter.wait();
            }
            assertEquals(1, counter.intValue() );
        }

        synchronized (counter) {
            counter.wait();
        }
        assertEquals(0, counter.intValue() );
    }

    @Test
    public void handleData() throws IOException {
        try (final Socket clientSocket = new Socket()) {
            clientSocket.connect(new InetSocketAddress(listenPort));

        }

    }

    @Before
    public void startServer() throws IOException {
        nioServer.start();
    }

    @After
    public void stopServer() throws IOException {
        nioServer.close();
    }

    private class MockChannelCallback implements ChannelCallback {


        @Override
        public void channelOpen() {
            synchronized (counter) {
                counter++;
                counter.notifyAll();
            }
        }

        @Override
        public void channelClose() {
            synchronized (counter) {
                counter--;
                counter.notifyAll();
            }
        }

        @Override
        public void channelData(ByteBuffer data) {

        }

    }


    private Integer counter = 0;
    private final NioServer nioServer;
    private static final int listenPort = 2345;
}
