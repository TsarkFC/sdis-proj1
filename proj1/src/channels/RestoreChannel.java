package channels;

import messages.Chunk;
import peer.Peer;
import protocol.RestoreProtocol;
import utils.AddressList;

import java.io.IOException;
import java.net.DatagramPacket;

public class RestoreChannel extends Channel{

    public RestoreChannel(AddressList addressList, Peer peer) {
        super(addressList, peer);
        super.currentAddr = addressList.getMdrAddr();

    }

    @Override
    public void handle(DatagramPacket packet) {
        //Como e que sei quantos chunks tenho?
        String rcvd = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Received in Restore Channel: " + rcvd);
        RestoreProtocol restoreProtocol = (RestoreProtocol) peer.getProtocol();
        restoreProtocol.handleChunkMsg(rcvd);

    }
}
