package protocol;

import messages.Delete;
import messages.PutChunk;
import peer.Peer;
import peer.PeerArgs;
import peer.metadata.FileMetadata;
import filehandler.FileHandler;
import utils.ThreadHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackupProtocol extends Protocol {
    final int repDgr;
    int repsLimit = 5;
    List<byte[]> messages;
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    int numOfChunks = 0;
    int timeWait = 1;
    int reps = 1;
    String fileId;

    public BackupProtocol(File file, Peer peer, int repDgr) {
        super(file, peer);
        this.repDgr = repDgr;
        System.out.println("though here");
        System.out.println(file.getName());
    }

    public BackupProtocol(String path, Peer peer, int repDgr) {
        super(path, peer);
        this.repDgr = repDgr;
    }


    @Override
    public void initialize()  {
        messages = new ArrayList<>();
        FileHandler fileHandler = new FileHandler(file);
        ConcurrentHashMap<Integer, byte[]> chunks = fileHandler.getFileChunks();
        fileId = fileHandler.createFileId();
        numOfChunks = chunks.size();

        if (peer.getMetadata().hasFile(fileId)) {
            System.out.println("File already backed up, aborting...");
            return;
        }

        // Updating a previously backed up file, delete previous one
        String previousFileId = peer.getMetadata().getFileIdFromPath(file.getPath());
        if (previousFileId != null) {
            System.out.println("Running DELETE protocol on previous file version...");

            PeerArgs peerArgs = peer.getPeerArgs();
            Delete msg = new Delete(peerArgs.getVersion(), peerArgs.getPeerId(), previousFileId);
            List<byte[]> msgs = new ArrayList<>();
            msgs.add(msg.getBytes());
            ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMcAddr().getAddress(),
                    peerArgs.getAddressList().getMcAddr().getPort(), msgs);

            System.out.println("[BACKUP] Received new version of file. Deleted previous one!");
        }

        FileMetadata fileMetadata = new FileMetadata(file.getPath(), fileId, repDgr, (int) file.length());
        peer.getMetadata().addHostingEntry(fileMetadata);

        // message initialization
        for (ConcurrentHashMap.Entry<Integer, byte[]> chunk : chunks.entrySet()) {
            PutChunk backupMsg = new PutChunk(peer.getPeerArgs().getVersion(), peer.getPeerArgs().getPeerId(), fileId,
                    chunk.getKey(), repDgr, chunk.getValue());
            messages.add(backupMsg.getBytes());
        }

        execute();
    }

    private void execute() {
        if (reps <= repsLimit) {
            ThreadHandler.startMulticastThread(peer.getPeerArgs().getAddressList().getMdbAddr().getAddress(),
                    peer.getPeerArgs().getAddressList().getMdbAddr().getPort(), messages);
            executor.schedule(this::verify, timeWait, TimeUnit.SECONDS);
            System.out.println("[BACKUP] Sent message, waiting " + timeWait + " seconds...");
        } else {
            System.out.println("[BACKUP] Reached resending limit of PUTCHUNK messages!");
            System.out.println("[FAILED] Failed to back up file...");
        }
    }

    private void verify() {
        if (!peer.getMetadata().verifyRepDgr(fileId, repDgr, numOfChunks)) {
            System.out.println("[BACKUP] Did not get expected replication degree after " + timeWait + " seconds. Resending...");
            reps++;
            timeWait *= 2;
            execute();
        } else {
            System.out.println("[BACKUP] Got expected replication degree!");
        }
    }

    public void backupChunk(String fileId, int chunkNo) throws IOException {
        messages = new ArrayList<>();
        FileHandler fileHandler = new FileHandler(file);

        if (peer.getMetadata().hasChunk(fileId, chunkNo)) {
            System.out.println("[BACKUP] File already backed up, aborting...");
            return;
        }
        FileMetadata fileMetadata = new FileMetadata(file.getPath(), fileId, repDgr, (int) file.length());
        peer.getMetadata().addHostingEntry(fileMetadata);

        PutChunk backupMsg = new PutChunk(peer.getPeerArgs().getVersion(), peer.getPeerArgs().getPeerId(), fileId,
                chunkNo, repDgr, fileHandler.getChunkFileData());
        messages.add(backupMsg.getBytes());

        execute();
    }
}
