import constants.TCPConstants;
import Provider.IoContext;
import Provider.impl.IoSelectorProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *  The Server, input "00bye00" to close
 */
public class Server {
    public static void main(String[] args) throws IOException {
        // Start IO
        IoContext.setup()
                .ioProvider(new IoSelectorProvider())
                .start();
        // TCP connection
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);

        boolean isSuccess = tcpServer.start();
        if(!isSuccess) {
            System.out.println("Cannot start server");
            return;
        }
        // UDP connection
        UDPProvider.start(TCPConstants.PORT_SERVER);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;

        do {
            // broadcast input
            str = bufferedReader.readLine();
            tcpServer.broadcast(str);
        } while(!"00bye00".equalsIgnoreCase(str));

        tcpServer.stop();
        UDPProvider.stop();

        IoContext.close();
    }
}