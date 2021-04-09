package protocol;

import messages.Chunk;
import messages.GetChunk;
import peer.Peer;
import utils.AddressList;
import filehandler.FileHandler;
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
        byte[] chunk = FileHandler.getChunk(rcvdMsg, peer.getFileSystem());
        if (chunk == null) {
            System.out.println("Peer does not have chunk " + rcvdMsg.getFileId() + "-" + rcvdMsg.getChunkNo() + ", aborting...");
            return;
        }
        List<byte[]> msgs = new ArrayList<>();
        Chunk msg = new Chunk(rcvdMsg.getVersion(), peer.getPeerArgs().getPeerId(), rcvdMsg.getFileId(),
                rcvdMsg.getChunkNo(), chunk);
        msgs.add(msg.getBytes());

        AddressList addrList = peer.getPeerArgs().getAddressList();
        ThreadHandler.startMulticastThread(addrList.getMdrAddr().getAddress(), addrList.getMdrAddr().getPort(), msgs);
    }
}