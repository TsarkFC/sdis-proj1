package channels;

import messages.Chunk;
import peer.Peer;
import utils.AddressList;

import java.io.IOException;
import java.net.DatagramPacket;

public class RestoreChannel extends Channel{

    public RestoreChannel(AddressList addrList, Peer peer) {
        super(addrList, peer);
        super.currentAddr = addrList.getMdrAddr();
    }

    @Override
    public void handle(DatagramPacket packet) throws IOException {
        String rcvd = new String(packet.getData(), 0, packet.getLength());
        Chunk chunkMsg = new Chunk(rcvd);
        System.out.println("Received in Restore Channel Chunk " + chunkMsg.getChunkNo());
    }
}
