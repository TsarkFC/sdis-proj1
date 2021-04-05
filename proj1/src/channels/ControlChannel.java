package channels;

import messages.Stored;
import peer.Peer;
import utils.AddressList;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.TimeUnit;

public class ControlChannel extends Channel {
    boolean isReceiving = true;

    public ControlChannel(AddressList addressList, Peer peer){
        super(addressList, peer);
        super.currentAddr = addressList.getMcAddr();
    }

    @Override
    public void handle(DatagramPacket packet) throws IOException {
        String rcvd = new String(packet.getData(), 0, packet.getLength());
        if (isReceiving) parseMsg(rcvd);
        else compareRepDgr();
    }

    public void parseMsg(String msgString) throws IOException {
        System.out.println("Control Channel received MBD Msg: " + msgString);
        Stored msg = new Stored(msgString);

        //Guardar em memoria nao volatil a quantidade de mensagens stored que recebeu de cada chunk
        peer.getPeerMetadata().updateChunkInfo(msg.getFileId(), msg.getChunkNo(), msg.getSenderId());
    }

    public void compareRepDgr(){

    }

    public void closeMcChannel(){
        System.out.println("Mc Channel stops receiving after 1 second");
    }




}
