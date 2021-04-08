package protocol;

import channels.BackupChannel;
import messages.Chunk;
import messages.GetChunk;
import messages.PutChunk;
import messages.Stored;
import peer.Peer;
import peer.PeerArgs;
import utils.AddressList;
import utils.FileHandler;
import utils.ThreadHandler;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


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

        if (!peer.getPeerMetadata().hasFile(fileId)) {
            System.out.println("Peer has not hosted BACKUP to file");
            return;
        }

        for (int i = 0; i < chunksNo; i++) {
            GetChunk getChunk = new GetChunk(peerArgs.getVersion(), peerArgs.getPeerId(), fileId, i);
            messages.add(getChunk.getBytes());
        }
        ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMcAddr().getAddress(),
                peerArgs.getAddressList().getMcAddr().getPort(), messages);
    }

    public static void handleGetChunkMsg(GetChunk rcvdMsg, Peer peer) {
        new ScheduledThreadPoolExecutor(1).schedule(new ChunkSender(rcvdMsg, peer), Utils.generateRandomDelay(), TimeUnit.MILLISECONDS);
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
