package ru.vkovalev.glance.server;

import ru.vkovalev.glance.server.io.NioServer;

import java.io.IOException;
import ru.vkovalev.glance.server.io.dto.*;

public class ServerMain {
    public static void main(String[] args) {
        try (final NioServer server = new NioServer(2345)) {
            server.start();
            server.join();
                // Sleep forever
        }
        catch (IOException e) {
            // Error while starting worker thread pool.
        }
        catch (InterruptedException e) {

        }
    }
}