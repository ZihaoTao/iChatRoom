package Packet;

public abstract class SendPacket extends Packet {
    private boolean isCancel = false;

    public abstract byte[] bytes();

    public boolean isCancel() {
        return isCancel;
    }
}
