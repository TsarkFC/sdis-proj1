package channels;

import utils.MulticastAddress;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public abstract class Channel implements Runnable {
    private final String mcastAddr;
    private final int mcastPort;

    public String getMcastAddr() { return mcastAddr; }

    public int getMcastPort() { return mcastPort; }

    public Channel(MulticastAddress mcastAddr){
        this.mcastAddr = mcastAddr.getAddress();
        this.mcastPort = mcastAddr.getPort();
    }

    public abstract void handle(DatagramPacket packet);

    @Override
    public void run() {
        try {
            InetAddress mcast_addr = InetAddress.getByName(this.mcastAddr);
            MulticastSocket mcast_socket = null;
            mcast_socket = new MulticastSocket(mcastPort);
            mcast_socket.joinGroup(mcast_addr);

            while(true){
                byte[] rbuf = new byte[256];
                DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
                mcast_socket.receive(packet);
                handle(packet);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
