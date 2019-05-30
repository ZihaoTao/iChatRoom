import constants.UDPConstants;
import utils.ByteUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

class UDPProvider {
    private static Provider PROVIDER_INSTANCE;

    static void start(int port) {
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, port);
        provider.start(); // thread
        PROVIDER_INSTANCE = provider;
    }

    static void stop() {
        if(PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    private static class Provider extends Thread {
        private final byte[] sn;
        private  final int port;
        private boolean done = false;
        private DatagramSocket ds = null;
        final byte[] buffer = new byte[128];

        private Provider(String sn, int port) {
            super();
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("UDPProvider started");

            try {
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);
                while(!done) {
                    ds.receive(receivePack);
                    String clientIp = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();
                    int clientDataLen = receivePack.getLength();
                    byte[] clientData = receivePack.getData();
                    // HEAD + cmd + callback
                    boolean isValid = clientDataLen >= (UDPConstants.HEADER.length + 2 + 4) &&
                            ByteUtils.startsWith(clientData, UDPConstants.HEADER);
                    System.out.println("UDPProvider receives from ip: " + clientIp
                            + "\tport: " + clientPort
                            + "\tdataValid: " + isValid);
                    if(!isValid) continue;
                    int index = UDPConstants.HEADER.length;
                    short cmd = (short) ((clientData[index++] << 8) | (clientData[index++] & 0xff));
                    // byte[] => int
                    int responsePort = ((clientData[index++] << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            (clientData[index] & 0xff));

                    // cmd 1: broadcast
                    if(cmd == 1 && responsePort > 0) {
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short) 2); // cmd
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        int len = byteBuffer.position();
                        DatagramPacket responsePacket = new DatagramPacket(buffer,
                                len,
                                receivePack.getAddress(),
                                responsePort);
                        ds.send(responsePacket);
                        System.out.println("UDPProvider response to: " + clientIp + "\tport: " + responsePort);
                    } else {
                        System.out.println("UDPProvider receive invalid cmd; cmd: " + cmd);
                    }
                }
            } catch(Exception ignored) {
            } finally {
                close();
            }

        }

        void exit() {
            done = true;
            close();
        }

        private void close() {
            if(ds != null) {
                ds.close();
                ds = null;
            }
        }
    }
}
