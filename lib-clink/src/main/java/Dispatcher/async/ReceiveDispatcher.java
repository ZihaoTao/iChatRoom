package Dispatcher.async;

import Packet.ReceivePacket;

import java.io.Closeable;

/**
 * received data package
 * Package one or more IoArgs
 */
public interface ReceiveDispatcher extends Closeable {
    void start();

    void stop();

    interface ReceivePacketCallback {
        void onReceivePacketCompleted(ReceivePacket packet);
    }
}
