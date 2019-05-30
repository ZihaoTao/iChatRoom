import bean.ServerInfo;
import constants.UDPConstants;
import utils.ByteUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class UDPSearcher {
    private static final int LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    static ServerInfo searchServer(int timeout) {
        System.out.println("UDPSearcher started");
        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener = null;

        try {
            listener = listen(receiveLatch);
            sendBroadcast();
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println("UDPSearcher finished");

        if(listener != null) {
            List<ServerInfo> servers = listener.getServerAndClose();
            if (servers.size() > 0) return servers.get(0);
        }

        return null;
    }

    private static class Listener extends Thread {
        private final int listenPort;
        private final CountDownLatch startDownLatch;
        private final CountDownLatch receiveDownLatch;
        private final List<ServerInfo> serverInfoList = new ArrayList<>();
        private final byte[] buffer = new byte[128];
        private final int minLen = UDPConstants.HEADER.length + 2 + 4; // header + cmd + callback
        private boolean done = false;
        private DatagramSocket ds = null;

        Listener(int listenPort, CountDownLatch startDownLatch, CountDownLatch receiveDownLatch) {
            super();
            this.listenPort = listenPort;
            this.startDownLatch = startDownLatch;
            this.receiveDownLatch = receiveDownLatch;
        }

        @Override
        public void run() {
            super.run();
            startDownLatch.countDown();

            try {
                ds = new DatagramSocket(listenPort);
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);
                while(!done) {
                    ds.receive(receivePack);
                    // get sender ip
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    byte[] data = receivePack.getData();
                    boolean isValid = dataLen >= minLen &
                            ByteUtils.startsWith(data, UDPConstants.HEADER);
                    System.out.println("UDPSearcher receive from ip: " + ip +
                            "\tport: " + port +
                            "\tdataValid: " + isValid);

                    if(!isValid) {
                        continue;
                    }

                    // resolve buffer
                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, UDPConstants.HEADER.length, dataLen);
                    final short cmd = byteBuffer.getShort();
                    final int serverPort = byteBuffer.getInt();
                    if(cmd != 2 || serverPort <= 0) {
                        System.out.println("UDPSearcher receive cmd: " + cmd + "\tserverPort: " + serverPort);
                    }

                    String sn = new String(buffer, minLen, dataLen - minLen);

                    ServerInfo server = new ServerInfo(sn, serverPort, ip);

                    serverInfoList.add(server);
                    receiveDownLatch.countDown();
                }
            } catch (Exception ignored) {
            } finally {
                close();
            }
            System.out.println("UDPSearcher listener finished");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        List<ServerInfo> getServerAndClose() {
            done = true;
            close();
            return serverInfoList;
        }
    }

    private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
        System.out.println("UDPSearcher Start to Listen");
        CountDownLatch startDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, startDownLatch, receiveLatch);
        listener.start();
        startDownLatch.await();
        return listener;
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher sendBroadCast Started");

        DatagramSocket ds = new DatagramSocket();

        // content of broadcast
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        byteBuffer.put(UDPConstants.HEADER);
        byteBuffer.putShort((short) 1); // cmd 1: broadcast
        byteBuffer.putInt(LISTEN_PORT);
        DatagramPacket requestPack = new DatagramPacket(byteBuffer.array(), byteBuffer.position() + 1);
        requestPack.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPack.setPort(UDPConstants.PORT_SERVER);

        ds.send(requestPack);
        ds.close();

        System.out.println("UDPSearcher sendBroadCast Finished");
    }
}

