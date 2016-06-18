package ru.vkovalev.glance.server.io;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.vkovalev.glance.server.ChannelCallback;
import ru.vkovalev.glance.server.ChannelCallbackFactory;
import ru.vkovalev.glance.server.threading.AutoCloseableThread;
import ru.vkovalev.glance.server.threading.ThreadCreationException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by vkovalev on 17.06.16.
 */
class NioWorkerThread extends AutoCloseableThread {

    NioWorkerThread( String name,
                     ServerSocketChannel serverSocketChannel,
                     ChannelCallbackFactory callbackFactory )
            throws ThreadCreationException {

        super.setName(name);
        this.channelCallbackFactory = callbackFactory;

        try {
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        }
        catch (IOException e) {
            throw new ThreadCreationException(e);
        }
        buffer = ByteBuffer.allocateDirect(4096);
        logger = LogManager.getLogger(this.getClass());
    }

    @Override
    public void run() {
        while ( ! isInterrupted() ) {
            try {
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
    public void close() {
        try {
            interrupt();
            try {
                join();
            }
            catch (InterruptedException e) {

            }
            finally {
                selector.close();
            }
        }
        catch (IOException e) {

        }
    }


    private void handleSelected(Set<SelectionKey> selectedKeys) throws IOException {
        for (final SelectionKey k : selectedKeys) {

            if (k.isAcceptable()) {
                final ServerSocketChannel channel = (ServerSocketChannel) k.channel();
                final SocketChannel client = channel.accept();
                if (client != null) {
                    client.configureBlocking(false);
                    final ChannelCallback callback = channelCallbackFactory.create();
                    client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, callback);
                }
            }
            if (k.isReadable()) {
                final SocketChannel channel = (SocketChannel) k.channel();
                final long readBytes = channel.read(buffer);
                final SocketChannel socketChannel = (SocketChannel)k.channel();
                final ChannelCallback callback = (ChannelCallback)k.attachment();
                if (readBytes == -1) {
                    callback.channelClose();
                }
                socketChannel.close();
            }
        }
    }

    private final ChannelCallbackFactory channelCallbackFactory;
    private final Logger logger;
    private final Selector selector;
    private final ByteBuffer buffer;
}
