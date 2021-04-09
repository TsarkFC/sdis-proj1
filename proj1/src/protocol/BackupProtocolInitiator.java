package protocol;

import messages.Removed;
import peer.Peer;
import peer.metadata.ChunkMetadata;
import utils.FileHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BackupProtocolInitiator implements Runnable {
    Removed removed;
    private ChunkMetadata chunkMetadata;
    private Peer peer;
    /**
     * //if during this delay, a peer receives a PUTCHUNK message for the same file chunk,
     * This map only has the fileId-chunkNo received before the reclaim initiated the protocol
     * if the chunk that is going to initiate backup is in the map, some peer already initiated the protocol
     */
    Set<String> receivedDuringReclaim = new HashSet<>();


    public BackupProtocolInitiator(Removed removed, ChunkMetadata chunkMetadata, Peer peer) {
        this.removed = removed;
        this.chunkMetadata = chunkMetadata;
        this.peer = peer;
    }

    public void run() {
        String path = FileHandler.getChunkPath(peer.getFileSystem(),removed.getFileId(),removed.getChunkNo());
        System.out.println("Initiating backup protocol of path: " + path);
        System.out.println();
        if(!receivedDuringReclaim(removed.getFileId(), removed.getChunkNo())){
            BackupProtocol backupProtocol = new BackupProtocol(path,peer,chunkMetadata.getRepDgr());
            try {
                backupProtocol.backupChunk(removed.getFileId() ,removed.getChunkNo());
            } catch (IOException e) {
                System.out.println("Exception initializing Backup protocol");
                e.printStackTrace();
            }
        }
        peer.getChannelCoordinator().setBackupInitiator(null);

    }
    public void setReceivedPutChunk(String fileId, int chunkNo){
        receivedDuringReclaim.add(fileId+"-"+chunkNo);
    }

    public boolean receivedDuringReclaim(String fileId,int chunkNo){
        return receivedDuringReclaim.contains(fileId+"-"+chunkNo);
    }
}