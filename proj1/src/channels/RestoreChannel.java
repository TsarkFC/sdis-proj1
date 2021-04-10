package channels;

import messages.Chunk;
import messages.ChunkEnhanced;
import peer.Peer;
import protocol.RestoreProtocol;
import utils.AddressList;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;

public class RestoreChannel extends Channel{

    public RestoreChannel(AddressList addressList, Peer peer) {
        super(addressList, peer);
        super.currentAddr = addressList.getMdrAddr();
    }

    @Override
    public void handle(DatagramPacket packet) {
        byte[] packetData = packet.getData();
        int bodyStartPos = getBodyStartPos(packetData);
        byte[] header = Arrays.copyOfRange(packetData, 0, bodyStartPos - 4);
        byte[] body = Arrays.copyOfRange(packetData, bodyStartPos, packet.getLength());

        String headerString = new String(header);

        if (peer.getArgs().getVersion() == 1.0) {
            Chunk msg = new Chunk(headerString, body);
            String chunkId = msg.getFileId() + "-" + msg.getChunkNo();
            peer.addChunkReceived(chunkId);

            RestoreProtocol restoreProtocol = (RestoreProtocol) peer.getProtocol();
            restoreProtocol.handleChunkMsg(msg);
        }
        else {
            ChunkEnhanced msg = new ChunkEnhanced(headerString);
            String chunkId = msg.getFileId() + "-" + msg.getChunkNo();
            peer.addChunkReceived(chunkId);

            RestoreProtocol restoreProtocol = (RestoreProtocol) peer.getProtocol();
            restoreProtocol.handleChunkEnhancedMsg(msg);
        }

    }
}
