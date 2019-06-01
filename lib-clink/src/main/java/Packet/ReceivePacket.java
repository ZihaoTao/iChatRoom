package Packet;

public abstract class ReceivePacket extends Packet {
    public abstract void save(byte[] bytes, int count);
}
