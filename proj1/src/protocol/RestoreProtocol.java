package protocol;

import messages.Chunk;
import messages.GetChunk;
import peer.Peer;
import peer.PeerArgs;
import utils.AddressList;
import utils.FileHandler;
import utils.ThreadHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class RestoreProtocol extends Protocol {
    private final Map<Integer, byte[]> chunksMap = new TreeMap<>();
    private int chunksNo;

    public RestoreProtocol(File file, Peer peer) {
        super(file, peer);
    }

    @Override
    public void initialize() {
        List<byte[]> messages = new ArrayList<>();
        FileHandler fileHandler = new FileHandler(file);
        chunksNo = fileHandler.getNumberOfChunks();
        String fileId = fileHandler.createFileId();
        PeerArgs peerArgs = peer.getPeerArgs();

        for (int i = 0; i < chunksNo; i++) {
            GetChunk getChunk = new GetChunk(peerArgs.getVersion(), peerArgs.getPeerId(), fileId, i);
            messages.add(getChunk.getBytes());
        }
        ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMcAddr().getAddress(),
                peerArgs.getAddressList().getMcAddr().getPort(), messages);
    }

    public static void handleGetChunkMsg(GetChunk rcvdMsg, Peer peer) {
        byte[] chunk = FileHandler.restoreChunk(rcvdMsg, peer.getFileSystem());
        if (chunk == null) {
            System.out.println("Peer does not have chunk " + rcvdMsg.getFileId() + "-" + rcvdMsg.getChunkNo() + ", aborting...");
            return;
        }

        List<byte[]> msgs = new ArrayList<>();
        Chunk msg = new Chunk(rcvdMsg.getVersion(), peer.getPeerArgs().getPeerId(), rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), chunk);
        msgs.add(msg.getBytes());
        System.out.println("Recovered chunk from: " + msg.getSenderId());

        //TODO: wait random delay between 0.jpg-400 ms
        AddressList addrList = peer.getPeerArgs().getAddressList();
        ThreadHandler.startMulticastThread(addrList.getMdrAddr().getAddress(), addrList.getMdrAddr().getPort(), msgs);
    }

    public void handleChunkMsg(Chunk rcvdMsg) throws IOException {
        System.out.println("Handling Chunk message...");
        chunksMap.put(rcvdMsg.getChunkNo(), rcvdMsg.getBody());

        if (chunksMap.size() >= chunksNo) {
            System.out.println("Backing up file...");
            String filename = peer.getRestoreDir() + "/" + file.getName();
            FileHandler.restoreFile(filename, chunksMap);
            System.out.println("RESTORE COMPLETE");

            //TODO: Stop receiving messages
        }
    }
}
