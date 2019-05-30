package handle;

import utils.CloseUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private final SocketChannel socketChannel;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final ClientHandlerCallback clientHandlerCallback;
    private final String clientInfo;


    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.socketChannel = socketChannel;
        // set NIO
        socketChannel.configureBlocking(false);

        Selector readSelector =  Selector.open();
        socketChannel.register(readSelector, SelectionKey.OP_READ);
        this.readHandler = new ClientReadHandler(readSelector);

        Selector writeSelector =  Selector.open();
        socketChannel.register(writeSelector, SelectionKey.OP_WRITE);
        this.writeHandler = new ClientWriteHandler(writeSelector);

        this.clientHandlerCallback = clientHandlerCallback;
        this.clientInfo = socketChannel.getRemoteAddress().toString();
        System.out.println("[Client] New Client " + clientInfo);
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void send(String str) {
        writeHandler.send(str);
    }

    public void readToPrint() {
        readHandler.start();
    }

    public void exit() {
        readHandler.exit();
        writeHandler.exit();
        CloseUtils.close(socketChannel);
        System.out.println("Client Exit: " + clientInfo);
    }

    private void exitBySelf() {
        exit();
        clientHandlerCallback .onSelfClosed(this);
    }

    public interface ClientHandlerCallback {
        // close by itself
        void onSelfClosed(ClientHandler clientHandler);
        // write msg and send
        void onNewMessageArrive(ClientHandler clientHandler, String msg);
    }

    class ClientReadHandler extends Thread {
        private boolean done = false;
        private final Selector selector;
        private final ByteBuffer byteBuffer;

        ClientReadHandler(Selector selector) {
            this.selector = selector;
            byteBuffer = ByteBuffer.allocate(256);
        }

        @Override
        public void run() {
            super.run();
            try {
                do {
                    // no selector available
                    if(selector.select() == 0) {
                        if(done) break;
                        continue;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    byteBuffer.clear();
                    while(iterator.hasNext()) {
                        if(done) break;
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if(key.isReadable()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            int read = client.read(byteBuffer);
                            if(read > 0) {
                                // remove line break
                                String str = new String(byteBuffer.array(), 0, byteBuffer.position() - 1);
                                // forward the msg to other clients
                                clientHandlerCallback.onNewMessageArrive(ClientHandler.this, str);
                            } else {
                                System.out.println("Client cannot read data");
                                ClientHandler.this.exitBySelf();
                                break;
                            }
                        }
                    }
                } while (!done);
            } catch (Exception e) {
                if(!done) {
                    System.out.println("[Error] " + e);
                    ClientHandler.this.exitBySelf();
                }
            } finally {
                CloseUtils.close(selector);
            }
        }

        void exit() {
            done = true;
            selector.wakeup();
            CloseUtils.close(selector);
        }
    }

    class ClientWriteHandler {
        private boolean done = false;
        private final Selector selector;
        private final ByteBuffer byteBuffer;
        private final ExecutorService executorService;

        ClientWriteHandler(Selector selector) {
            this.selector = selector;
            this.byteBuffer = ByteBuffer.allocate(256);
            this.executorService = Executors.newSingleThreadExecutor();
        }

        void exit() {
            done = true;
            CloseUtils.close(selector);
            executorService.shutdownNow();
        }

        void send(String str) {
            if(done) return;
            executorService.execute(new WriteRunnable(str));
        }

        class WriteRunnable implements Runnable {
            private final String str;

            WriteRunnable(String str) {
                this.str = str;
            }

            @Override
            public void run() {
                if (ClientWriteHandler.this.done) return;
                byteBuffer.clear();
                // pointer is at the end of bytes
                byteBuffer.put(str.getBytes());
                // flip byteBuffer
                // send data from pointer, flip to make sure pointer is at the head
                byteBuffer.flip();
                // check if byteBuffer still has data
                while (!done && byteBuffer.hasRemaining()) {
                    try {
                        int len = socketChannel.write(byteBuffer);
                        // len == 0 is legal
                        if(len < 0) {
                            System.out.println("Server cannot send data");
                            ClientHandler.this.exitBySelf();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
