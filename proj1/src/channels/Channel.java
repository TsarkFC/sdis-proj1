package channels;

import peer.Peer;
import utils.AddressList;
import utils.MulticastAddress;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public abstract class Channel implements Runnable {
    protected AddressList addrList;
    protected MulticastAddress currentAddr;
    protected Peer peer;
    private final int CR = 0xD;
    private final int LF = 0xA;
    private final double MAX_SIZE = Math.pow(2, 16);

    public AddressList getAddrList() {
        return addrList;
    }

    public Channel(AddressList addrList, Peer peer) {
        this.addrList = addrList;
        this.peer = peer;
    }
    public abstract void handle(DatagramPacket packet) throws IOException;

    @Override
    public void run() {
        try {
            InetAddress mcastAddr = InetAddress.getByName(this.currentAddr.getAddress());
            MulticastSocket mcastSocket;
            mcastSocket = new MulticastSocket(currentAddr.getPort());
            mcastSocket.joinGroup(mcastAddr);

            while (true) {
                byte[] rbuf = new byte[(int) MAX_SIZE];
                DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
                mcastSocket.receive(packet);
                handle(packet);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected int getBodyStartPos(byte[] msg) {
        int crlf = 0;
        for (int i = 0; i < msg.length - 1; i++) {
            if (msg[i] == CR && msg[i+1] == LF && crlf == 1) {
                return i + 2;
            } else if (msg[i] == CR && msg[i+1] == LF) {
                crlf++;
                i++;
            } else crlf = 0;
        }
        return 0;
    }
}
