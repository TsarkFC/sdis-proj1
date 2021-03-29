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
        PutChunk msg = new PutChunk(recv);

        if (msg.getSenderId() != peer.getPeerArgs().getPeerId()) {
            FileHandler.saveChunk(msg, peer.getFileSystem());

            //If parse correctly, send stored msg to MC channel
            Stored confmsg = new Stored(msg.getVersion(), msg.getSenderId(),
                    msg.getFileId(), msg.getChunkNo());
            sendConfirmationMc(confmsg.getBytes());
        }
    }

    public void sendConfirmationMc(byte[] msg) {
        List<byte[]> msgs = new ArrayList<>();
        msgs.add(msg);
        ThreadHandler.startMulticastThread(addrList.getMcAddr().getAddress(), addrList.getMcAddr().getPort(), msgs);
    }
}
