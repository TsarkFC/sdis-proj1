import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class BackupChannel implements Runnable {

    private final String mcast_addr;
    private final int mcast_port;


    public BackupChannel(int mcast_port, String mcast_addr){
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;

    }


    @Override
    public void run() {

        try {
            InetAddress mcast_addr = InetAddress.getByName(this.mcast_addr);
            MulticastSocket mcast_socket = null;
            mcast_socket = new MulticastSocket(mcast_port);
            mcast_socket.joinGroup(mcast_addr);
            byte[] rbuf = new byte[256];

            DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
            mcast_socket.receive(packet);

            String rcvd = new String(packet.getData(), 0, packet.getLength());
            System.out.println("All peers recieve MBD Msg: " +rcvd);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
