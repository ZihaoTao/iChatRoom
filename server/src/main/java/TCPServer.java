import handle.ClientHandler;
import utils.CloseUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class TCPServer implements ClientHandler.ClientHandlerCallback{
    private final int port;
    private ClientListener listener;
    private List<ClientHandler> clientHandlerList;
    private final ExecutorService forwardingThreadPoolExecutor;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    TCPServer(int port) {
        this.port = port;
        clientHandlerList = new ArrayList<>();
        forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    boolean start() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            // set NIO
            serverSocketChannel.configureBlocking(false);
            // bind local port
            serverSocketChannel.bind(new InetSocketAddress(port));
            // start listening
            ClientListener listener = new ClientListener();
            // register event
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server Info: " + serverSocketChannel.getLocalAddress().toString());
            this.listener = listener;
            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    void stop() {
        if(listener != null) {
            listener.exit();
        }
        // close channel and selector
        CloseUtils.close(serverSocketChannel);
        CloseUtils.close(selector);


        // close all clientHandler
        synchronized (TCPServer.this) {

            for (ClientHandler i : clientHandlerList) {
                i.exit();
            }

            clientHandlerList.clear();
        }
        // close thread pool
        forwardingThreadPoolExecutor.shutdownNow();
    }

    synchronized void broadcast(String str) {
        for(ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(str);
        }
    }

    @Override
    public synchronized void onSelfClosed(ClientHandler clientHandler) {
        clientHandlerList.remove(clientHandler);
    }

    @Override
    public void onNewMessageArrive(final ClientHandler handler, final String msg) {
        forwardingThreadPoolExecutor.execute(() -> {
            synchronized (TCPServer.this) {
                for (ClientHandler clientHandler : clientHandlerList) {
                    if (clientHandler.equals(handler)) {
                        continue;
                    }
                    clientHandler.send(msg);
                }
            }
        });
    }

    private class ClientListener extends Thread{
        private boolean done = false;

        @Override
        public void run() {
            super.run();

            Selector selector = TCPServer.this.selector;

            System.out.println("Preparation finished");

            do {
                try {
                    if(selector.select() == 0) {
                        if(done) break;
                        continue;
                    }

                    // get all NIO channels
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while(iterator.hasNext()) {
                        if(done) break;
                        SelectionKey key = iterator.next();
                        iterator.remove(); // because of iterator, wont cause trouble
                        // check the status of key, if the specific client has sent data to server
                        if(key.isAcceptable()) {
                            // the channel that you set ahead
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            // even if it is IO operation, it must be NIO / (isAcceptable)
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            try {
                                ClientHandler clientHandler = new ClientHandler(socketChannel, TCPServer.this);
                                synchronized (TCPServer.this) {
                                    clientHandlerList.add(clientHandler);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("Server Error: " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (!done);

            System.out.println("Server closed");
        }

        void exit() {
            done = true;
            // return and close the selector, even if it is in IO status
            selector.wakeup();
        }
    }
}