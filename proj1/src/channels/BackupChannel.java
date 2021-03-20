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

public class BackupChannel  extends Channel  {

    public BackupChannel(){
        super(MDB_PORT,MDB_HOSTNAME);
    }

    @Override
    public void handle(DatagramPacket packet) {
        String rcvd = new String(packet.getData(), 0, packet.getLength());
        System.out.println("All peers recieve MBD Msg: " +rcvd);
        Message message = parseMsg(rcvd);
        //If parse correctly, send stored msg to MC channel
        sendConfirmationMc(message);
    }

    public Message parseMsg(String msgString){
        Message msg = new PutChunk(msgString);
        System.out.println(msg.getMessageType());
        return msg;
    }

    public void sendConfirmationMc(Message backupMsg){
        //TODO Alterar para ter senderId direito i guess
        //TODO Adicionar nova classe multicast que nao receba uma lista?
        Stored storedMsg = new Stored(backupMsg.getVersion(), backupMsg.getSenderId(), backupMsg.getFileId(), backupMsg.getChunkNo());
        List<String> messages = new ArrayList<>();
        messages.add(storedMsg.getMsgString());
        ThreadHandler.startMulticastThread(Channel.getMcHostname(),Channel.getMcPort(),messages);
    }


}