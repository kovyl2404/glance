package ru.vkovalev.glance.server;

/**
 * Created by vkovalev on 17.06.16.
 */
public interface ChannelCallback <Channel> {
    void channelOpen(Channel channel);
    void channelClose(Channel channel);
}
