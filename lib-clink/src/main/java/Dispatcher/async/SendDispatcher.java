package Dispatcher.async;

import Packet.SendPacket;

import java.io.Closeable;

/**
 * put data into buffer, use queue to send data
 * package data to IoArgs, which can be solved by Sender
 */
public interface SendDispatcher extends Closeable {
    /**
     * Send data
     * @param packet
     */
    void send(SendPacket packet);

    /**
     * Cancel sending
     * @param packet
     */
    void cancel(SendPacket packet);
}
