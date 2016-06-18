package ru.vkovalev.glance.server;

import java.nio.ByteBuffer;

/**
 * Created by vkovalev on 17.06.16.
 */
public interface ChannelCallback {
    void channelOpen();
    void channelClose();
    void channelData(ByteBuffer data);
}
