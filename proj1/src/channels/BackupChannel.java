package channels;

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
        System.out.println("Length: " + packet.getLength());
        System.out.println("Received message from MDB channel");
        PutChunk message = new PutChunk(recv);
        FileHandler.saveChunk(message, peer.getFileSystem());

        //If parse correctly, send stored msg to MC channel
        Stored confMessage = new Stored(message.getVersion(), message.getSenderId(),
                message.getFileId(), message.getChunkNo());
        sendConfirmationMc(confMessage.getBytes());
    }

    public void sendConfirmationMc(byte[] message) {
        List<byte[]> messages = new ArrayList<>();
        messages.add(message);
        ThreadHandler.startMulticastThread(addrList.getMcAddr().getAddress(), addrList.getMcAddr().getPort(), messages);
    }
}
