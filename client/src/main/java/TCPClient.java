import bean.ServerInfo;
import core.Connector;
import utils.CloseUtils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

class TCPClient extends Connector {

    TCPClient(SocketChannel socketChannel) throws IOException {
        setup(socketChannel);
    }

    void exit() {
        CloseUtils.close(this);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        System.out.println("Connection closed, cannot read data.");
    }

    static TCPClient startWith(ServerInfo serverInfo) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();

        System.out.println("Begin to connect server, ");
        socketChannel.connect(new InetSocketAddress(Inet4Address.getByName(serverInfo.getAddress()), serverInfo.getPort()));

        System.out.println("Client Info: " + socketChannel.getLocalAddress().toString());
        System.out.println("Server Info: " + socketChannel.getRemoteAddress().toString());

        try {
            return new TCPClient(socketChannel);
        } catch (Exception e) {
            System.out.println("[Error] " + e);
            CloseUtils.close(socketChannel);
        }
        return null;
    }
}
