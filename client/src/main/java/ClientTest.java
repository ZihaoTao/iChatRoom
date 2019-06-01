import bean.ServerInfo;
import Provider.IoContext;
import Provider.impl.IoSelectorProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientTest {
    private static boolean done = false;
    public static void main(String[] args) throws IOException {
        ServerInfo info = UDPSearcher.searchServer(10000);
        System.out.println("Server:" + info);
        if(info == null) return;
        int size = 0;
        List<TCPClient> list = new ArrayList<>();

        IoContext.setup()
                .ioProvider(new IoSelectorProvider())
                .start();


        for(int i = 0; i < 5; i++) {
            try {
                TCPClient tcpClient = TCPClient.startWith(info);
                if(tcpClient == null) {
                    System.out.println("Connection Error");
                    continue;
                }
                list.add(tcpClient);
                System.out.println("Connection succeed: " + (++size) + " connections");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.in.read();
        Runnable runnable = () -> {
            while(!done) {
                for(TCPClient tcpClient : list) {
                    if(tcpClient != null) {
                        tcpClient.send("Hello");
                    }
                }
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        System.in.read();
        done = true;
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(TCPClient tcpClient: list) {
            tcpClient.exit();
        }

        IoContext.close();
    }
}
