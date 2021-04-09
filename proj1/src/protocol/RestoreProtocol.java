package protocol;

import messages.Chunk;
import messages.GetChunk;
import peer.Peer;
import peer.PeerArgs;
import filehandler.FileHandler;
import peer.metadata.Metadata;
import utils.AddressList;
import utils.ThreadHandler;
import utils.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class RestoreProtocol extends Protocol {
    private final ConcurrentHashMap<Integer, byte[]> chunksMap = new ConcurrentHashMap<>();
    private int chunksNo;
    private boolean complete = false;

    public RestoreProtocol(String path, Peer peer) {
        super(path, peer);
    }

    @Override
    public void initialize() {
        List<byte[]> messages = new ArrayList<>();
        Metadata metadata = peer.getMetadata();
        String fileId = metadata.getFileIdFromPath(path);
        PeerArgs peerArgs = peer.getPeerArgs();

        if (!metadata.hasFile(fileId)) {
            System.out.println("Peer has not hosted BACKUP to file");
            return;
        }
        chunksNo = FileHandler.getNumberOfChunks(metadata.getFileSize(fileId));
        System.out.println("CHUNKno: " + chunksNo);

        for (int i = 0; i < chunksNo; i++) {
            GetChunk getChunk = new GetChunk(peerArgs.getVersion(), peerArgs.getPeerId(), fileId, i);
            messages.add(getChunk.getBytes());
        }
        ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMcAddr().getAddress(),
                peerArgs.getAddressList().getMcAddr().getPort(), messages);
    }

    public static void handleGetChunkMsg(GetChunk rcvdMsg, Peer peer) {
        new ScheduledThreadPoolExecutor(1).
                schedule(() -> sendChunk(rcvdMsg, peer), Utils.generateRandomDelay(), TimeUnit.MILLISECONDS);
    }

    public static void sendChunk(GetChunk rcvdMsg, Peer peer) {
        byte[] chunk = FileHandler.getChunk(rcvdMsg, peer.getFileSystem());
        if (chunk == null) {
            return;
        }
        List<byte[]> msgs = new ArrayList<>();
        Chunk msg = new Chunk(rcvdMsg.getVersion(), peer.getPeerArgs().getPeerId(), rcvdMsg.getFileId(),
                rcvdMsg.getChunkNo(), chunk);
        msgs.add(msg.getBytes());

        AddressList addrList = peer.getPeerArgs().getAddressList();
        ThreadHandler.startMulticastThread(addrList.getMdrAddr().getAddress(), addrList.getMdrAddr().getPort(), msgs);
    }

    public void handleChunkMsg(Chunk rcvdMsg) {
        if (complete) {
            return;
        }
        chunksMap.put(rcvdMsg.getChunkNo(), rcvdMsg.getBody());

        if (chunksMap.size() >= chunksNo) {
            Path restoreFilePath = Paths.get(path);
            String filename = peer.getRestoreDir() + "/" + restoreFilePath.getFileName();
            FileHandler.restoreFile(filename, chunksMap);
            complete = true;
            System.out.println("[RESTORE] completed");
        }
    }


}
