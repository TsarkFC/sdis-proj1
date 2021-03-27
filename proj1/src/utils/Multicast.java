package utils;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class Multicast implements Runnable {

    private final String mcast_addr;
    private final int mcast_port;
    private final List<String> messages;

    public Multicast(int mcast_port, String mcast_addr, List<String> messages) {
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.messages = messages;
    }


    @Override
    public void run() {
        //Aqui e que ele vai decidir a que peers e que manda
        MulticastSocket socket;
        try {
            socket = new MulticastSocket();
            InetAddress group = InetAddress.getByName(mcast_addr);
            for (String msg : messages) {
                byte[] buf = msg.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, group, mcast_port);
                socket.send(datagramPacket);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
