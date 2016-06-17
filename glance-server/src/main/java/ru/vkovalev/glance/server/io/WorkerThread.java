package ru.vkovalev.glance.server.io;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.vkovalev.glance.server.ChannelCallback;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * Created by vkovalev on 17.06.16.
 */
class WorkerThread extends Thread implements AutoCloseable{

    WorkerThread(int index) throws IOException {
        super.setName(String.format("NioServer worker thread %d", index));
        selector = Selector.open();
        registrationRequests = new LinkedList<>();
        buffer = ByteBuffer.allocateDirect(4096);
        logger = LogManager.getLogger(this.getClass());
    }

    @Override
    public void run() {
        while ( ! isInterrupted() ) {
            try {
                processRegistrations();
                final int selectedCount = selector.select();
                if ( selectedCount == 0 ) {
                    continue;
                }
                handleSelected(selector.selectedKeys());
            }
            catch (IOException e) {
                // This is very platform depending.
                // It's worth to continue.
            }
        }
    }

    @Override
    public void close() throws IOException {
        selector.close();
    }

    private void processRegistrations() {
        synchronized (registrationRequests) {
            for (final RegistrationRequest r: registrationRequests) {
                try {
                    r.handle();
                }
                catch (IOException e) {

                }
            }
        }
    }

    private void handleSelected(Set<SelectionKey> selectedKeys) throws IOException {
        for (final SelectionKey k : selectedKeys) {
            final SocketChannel channel = (SocketChannel) k.channel();
            if (k.isReadable()) {
                final long readBytes = channel.read(buffer);
                if (readBytes == -1) {
                    ChannelCallback<SocketChannel> callback = (ChannelCallback<SocketChannel>)k.attachment();
                    if (callback != null) {
                        callback.channelClose(channel);
                    }
                    channel.close();

                }
            }
        }
    }

    void register(
            SocketChannel socketChannel,
            ChannelCallback<SocketChannel> channelCallback )
                throws IOException {

        synchronized (registrationRequests) {
            registrationRequests.add(new RegistrationRequest(socketChannel, channelCallback));
        }
        channelCallback.channelOpen(socketChannel);
        selector.wakeup();

    }

    private class RegistrationRequest {
        private RegistrationRequest(SocketChannel channel, ChannelCallback<SocketChannel> callback) {
            this.channel = channel;
            this.callback = callback;
        }
        public void handle() throws IOException {
            channel.configureBlocking(false);
            channel.register( selector,
                                SelectionKey.OP_READ | SelectionKey.OP_WRITE,
                                callback );
        }
        private final SocketChannel channel;
        private final ChannelCallback<SocketChannel> callback;
    }


    private final List<RegistrationRequest> registrationRequests;
    private final Logger logger;
    private final Selector selector;
    private final ByteBuffer buffer;
}
