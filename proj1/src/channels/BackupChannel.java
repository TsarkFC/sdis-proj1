package channels;

import messages.CoordMessage;
import messages.Message;
import messages.PutChunk;
import messages.Stored;
import peer.Peer;
import utils.AddressList;
import utils.FileHandler;
import utils.ThreadHandler;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

public class BackupChannel extends Channel {

    public BackupChannel(AddressList addressList, Peer peer) {
        super(addressList, peer);
        super.currentAddr = addressList.getMdbAddr();
    }

    @Override
    public void handle(DatagramPacket packet) {
        String recv = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Length \n\n\n" +packet.getLength());
        System.out.println("Received message from MDB channel: " + recv + "\n");
        Message message = new PutChunk(recv);
        FileHandler.saveChunk(message, peer.getFileSystem());

        //If parse correctly, send stored msg to MC channel
        sendConfirmationMc(message);
    }

    public void sendConfirmationMc(Message backupMsg) {
        //TODO Alterar para ter senderId direito i guess
        //TODO Adicionar nova classe multicast que nao receba uma lista?
        Stored storedMsg = new Stored(backupMsg.getVersion(), backupMsg.getSenderId(), backupMsg.getFileId(), backupMsg.getChunkNo());
        List<byte[]> messages = new ArrayList<>();
        messages.add(storedMsg.getMsgBytes());
        ThreadHandler.startMulticastThread(addrList.getMcAddr().getAddress(), addrList.getMcAddr().getPort(), messages);
        //Esperar um segundo
        //Enviar mensagem um segundo
    }
}
