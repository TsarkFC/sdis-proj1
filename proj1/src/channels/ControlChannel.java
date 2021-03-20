package channels;

import messages.Message;
import messages.PutChunk;
import messages.Stored;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ControlChannel extends Channel {


    public ControlChannel(){
        super(Channel.MC_PORT,Channel.MC_HOSTNAME);
    }

    @Override
    public void handle(DatagramPacket packet) {
        String rcvd = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Control Channel received MBD Msg: " + rcvd);
        parseMsg(rcvd);
    }

    public void parseMsg(String msgString){
        Message msg = new Stored(msgString);
        System.out.println(msg.getMessageType());
    }

}
