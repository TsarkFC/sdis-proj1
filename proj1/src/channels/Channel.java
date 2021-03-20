package channels;

import messages.Message;
import messages.PutChunk;
import messages.Stored;
import utils.ThreadHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public abstract class Channel implements Runnable {

    //Pode ter um construtor que inicializa as variaveis

    protected final static  String MC_HOSTNAME="228.25.25.25";
    protected final static int  MC_PORT=4445;
    protected final static  String MDB_HOSTNAME="228.25.25.25";
    protected final static int  MDB_PORT=4446;
    protected final static  String MDC_HOSTNAME="228.25.25.25";
    protected final static int  MDC_PORT=4447;

    private final String mcast_addr;
    private final int mcast_port;

    public static String getMcHostname() {
        return MC_HOSTNAME;
    }
    public static int getMcPort() {
        return MC_PORT;
    }
    public static String getMdbHostname() {
        return MDB_HOSTNAME;
    }
    public static int getMdbPort() {
        return MDB_PORT;
    }
    public static String getMdcHostname() {
        return MDC_HOSTNAME;
    }
    public static int getMdcPort() {
        return MDC_PORT;
    }




    public Channel(int mcast_port, String mcast_addr){
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;

    }

    public abstract void handle(DatagramPacket packet);


    @Override
    public void run() {

        try {
            InetAddress mcast_addr = InetAddress.getByName(this.mcast_addr);
            MulticastSocket mcast_socket = null;
            mcast_socket = new MulticastSocket(mcast_port);
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
