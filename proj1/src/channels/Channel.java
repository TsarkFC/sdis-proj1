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
                byte[] rbuf = new byte[64000];
                DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
                mcastSocket.receive(packet);
                handle(packet);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
