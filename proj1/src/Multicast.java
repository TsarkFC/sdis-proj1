import java.io.IOException;
import java.net.*;

public class Multicast implements Runnable {

    private final String mcast_addr;
    private final int mcast_port;
    private final String message;

    public Multicast(int mcast_port, String mcast_addr, String message){
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.message = message;
    }

    public void multicast(String message) throws IOException {

    }

    public void printMulticastMsg(){
        System.out.println("Multicast: " +mcast_addr + " " + mcast_port );
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress group = InetAddress.getByName(mcast_addr);
            byte[] buf = message.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(buf,buf.length,group,mcast_port);
            socket.send(datagramPacket);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
