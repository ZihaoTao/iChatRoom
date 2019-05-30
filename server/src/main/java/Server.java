import constants.TCPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {
    public static void main(String[] args) throws IOException {
        // TCP connection
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);

        boolean isSuccess = tcpServer.start();
        if(!isSuccess) {
            System.out.println("Cannot start server");
            return;
        }
        // UDP connect
        UDPProvider.start(TCPConstants.PORT_SERVER);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;

        do {
            // broadcast input
            str = bufferedReader.readLine();
            tcpServer.broadcast(str);
        } while(!"00bye00".equalsIgnoreCase(str));

        System.out.println(str);
        tcpServer.stop();
        UDPProvider.stop();
    }
}