import bean.ServerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Client {
    private static final int TIMEOUT = 30000;
    public static void main(String[] args) {
        // get server address
        ServerInfo info = UDPSearcher.searchServer(TIMEOUT);
        System.out.println("Server:" + info);

        if(info != null) {
            TCPClient tcpClient = null;
            try {
                // pass server address to get TCP link
                 tcpClient = TCPClient.startWith(info);
                 if(tcpClient == null) return;
                 write(tcpClient);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(tcpClient != null) {
                    tcpClient.exit();
                }
            }
        }
    }

    private static void write(TCPClient tcpClient) throws IOException {
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));
        do {
            String str = input.readLine();
            tcpClient.send(str);
            if (str == null || "00bye00".equalsIgnoreCase(str)) {
                break;
            }
        } while(true);
    }
}

