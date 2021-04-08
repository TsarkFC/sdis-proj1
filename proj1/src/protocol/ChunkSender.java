package protocol;

import messages.Chunk;
import messages.GetChunk;
import peer.Peer;
import utils.AddressList;
import utils.FileHandler;
import utils.ThreadHandler;

import java.util.ArrayList;
import java.util.List;

public class ChunkSender implements Runnable {
    GetChunk rcvdMsg;
    Peer peer;


    public ChunkSender(GetChunk rcvdMsg, Peer peer) {
        this.rcvdMsg = rcvdMsg;
        this.peer = peer;
    }

    public void run() {
        byte[] chunk = FileHandler.restoreChunk(rcvdMsg, peer.getFileSystem());
        if (chunk == null) {
            System.out.println("Peer does not have chunk " + rcvdMsg.getFileId() + "-" + rcvdMsg.getChunkNo() + ", aborting...");
            return;
        }
        List<byte[]> msgs = new ArrayList<>();
        Chunk msg = new Chunk(rcvdMsg.getVersion(), peer.getPeerArgs().getPeerId(), rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), chunk);
        msgs.add(msg.getBytes());
        System.out.println("Recovered chunk from: " + msg.getSenderId());

        AddressList addrList = peer.getPeerArgs().getAddressList();
        ThreadHandler.startMulticastThread(addrList.getMdrAddr().getAddress(), addrList.getMdrAddr().getPort(), msgs);
    }
}