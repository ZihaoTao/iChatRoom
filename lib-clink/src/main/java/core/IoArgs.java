package core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class IoArgs {
    private int limit = 256;
    private byte[] byteBuffer = new byte[256];
    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);

    /**
     * Get data from bytes array
     */
    public int readFrom(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.put(bytes, offset, size);
        return size;
    }

    /**
     * Put data to bytes array
     */
    public int writeTo(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.get(bytes, offset, size);
        return size;
    }

    /**
     * Get data from SocketChannel
     */
    public int readFrom(SocketChannel channel) throws IOException {
        startWriting();
        int bytesProduced = 0;

        while(buffer.hasRemaining()) {
            int len = channel.read(buffer);
            if(len < 0) throw new EOFException();
            bytesProduced += len;
        }

        finishWriting();
        return bytesProduced;

    }

    /**
     * Start to write data to IoArgs
     */
    public void startWriting() {
        buffer.clear();
        // Set the limit of buffer
        buffer.limit(limit);
    }

    /**
     * Flip buffer to read;
     */
    public void finishWriting() {
        buffer.flip();
    }

    /**
     * Method to set limit of buffer
     * @param limit
     */
    public void limit(int limit) {
        this.limit = limit;
    }

    public void writeLength(int total) {
        buffer.putInt(total);
    }

    public int readLength() {
        return buffer.getInt();
    }

    public int capacity() {
        return buffer.capacity();
    }
    /**
     * Put data to SocketChannel
     */
    public int writeTo(SocketChannel channel) throws IOException {
        int bytesProduced = 0;

        while(buffer.hasRemaining()) {
            int len = channel.write(buffer);
            if(len < 0) throw new EOFException();
            bytesProduced += len;
        }

        return bytesProduced;
    }

    public interface IoArgsEventListener {
        void onStarted(IoArgs args);
        void onCompleted(IoArgs args);
    }
}
