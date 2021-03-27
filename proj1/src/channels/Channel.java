package channels;

import messages.CoordMessage;
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

    public abstract void handle(DatagramPacket packet);

    @Override
    public void run() {
        try {
            InetAddress mcast_addr = InetAddress.getByName(this.currentAddr.getAddress());
            MulticastSocket mcast_socket;
            mcast_socket = new MulticastSocket(currentAddr.getPort());
            mcast_socket.joinGroup(mcast_addr);

            while (true) {
                byte[] rbuf = new byte[64000];
                DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
                mcast_socket.receive(packet);
                handle(packet);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
