import bean.ServerInfo;
import utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

class TCPClient {
    private final Socket socket;
    private final ReadHandler readHandler;
    private final PrintStream printStream;

    TCPClient(Socket socket, ReadHandler readHandler) throws IOException {
        this.socket = socket;
        this.readHandler = readHandler;
        printStream = new PrintStream(socket.getOutputStream());
    }

    void exit() {
        readHandler.exit();
        CloseUtils.close(printStream);
        CloseUtils.close(socket);
    }

    void send(String msg) {
        printStream.println(msg);
    }

    static TCPClient startWith(ServerInfo serverInfo) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(3000);
        System.out.println("Begin to connect server, ");
        socket.connect(new InetSocketAddress(Inet4Address.getByName(serverInfo.getAddress()), serverInfo.getPort()), 3000);

        System.out.println("Client Info: " + socket.getLocalAddress() + " P: " + socket.getLocalPort());
        System.out.println("Server Info: " + socket.getInetAddress() + " P: " + socket.getPort());

        try {
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            readHandler.start();
            return new TCPClient(socket, readHandler);
        } catch (Exception e) {
            System.out.println("[Error] " + e);
            CloseUtils.close(socket);
        }
        return null;
    }


    // Client system in
    static class ReadHandler extends Thread {
        private boolean done = false;
        private final InputStream inputStream;

        ReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            super.run();
            try {
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));
                do {
                    String str;
                    try {
                        str = socketInput.readLine();
                    } catch (SocketTimeoutException e) {
                        continue;
                    }

                    if (str == null) {
                        System.out.println("Connection closed");
                        break;
                    }

                    System.out.println(str);
                } while (!done);
            } catch (Exception e) {
                if(!done) {
                    System.out.println("[Error] " + e.getMessage());
                }
            } finally {
                CloseUtils.close(inputStream);
            }
        }

        void exit() {
            done = true;
            CloseUtils.close(inputStream);
        }
    }
}
