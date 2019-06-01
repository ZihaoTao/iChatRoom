package handle;

import core.Connector;
import utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 *  Build socketChannel and provide callback for outside
 *
 */
public class ClientHandler extends Connector{
    private final ClientHandlerCallback clientHandlerCallback;
    private final String clientInfo;


    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.clientHandlerCallback = clientHandlerCallback;
        this.clientInfo = socketChannel.getRemoteAddress().toString();
        System.out.println("[Client] New Client " + clientInfo);
        setup(socketChannel);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exitBySelf();
    }

    private void exitBySelf() {
        exit();
        clientHandlerCallback.onSelfClosed(this);
    }

    public void exit() {
        CloseUtils.close(this);
        System.out.println("Client Exit: " + clientInfo);
    }

    @Override
    protected void onReceiveNewMessage(String str) {
        super.onReceiveNewMessage(str);
        clientHandlerCallback.onNewMessageArrive(this, str);
    }

    public interface ClientHandlerCallback {
        // close by itself
        void onSelfClosed(ClientHandler clientHandler);
        // writeTo msg and send
        void onNewMessageArrive(ClientHandler clientHandler, String msg);
    }
}
