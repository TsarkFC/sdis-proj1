package protocol;

import messages.Delete;
import messages.PutChunk;
import peer.Peer;
import peer.PeerArgs;
import peer.metadata.FileMetadata;
import utils.FileHandler;
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
    }

    public BackupProtocol(String path, Peer peer, int repDgr) {
        super(path, peer);
        this.repDgr = repDgr;
    }


    @Override
    public void initialize() throws IOException {
        messages = new ArrayList<>();
        FileHandler fileHandler = new FileHandler(file);
        ConcurrentHashMap<Integer, byte[]> chunks = fileHandler.splitFile();
        fileId = fileHandler.createFileId();
        numOfChunks = chunks.size();

        if (peer.getPeerMetadata().hasFile(fileId)) {
            System.out.println("File already backed up, aborting...");
            return;
        }

        // Updating a previously backed up file, delete previous one
        String previousFileId = peer.getPeerMetadata().getFileIdFromPath(file.getPath());
        if (previousFileId != null) {
            System.out.println("Running DELETE protocol on previous file version...");

            PeerArgs peerArgs = peer.getPeerArgs();
            Delete msg = new Delete(peerArgs.getVersion(), peerArgs.getPeerId(), previousFileId);
            List<byte[]> msgs = new ArrayList<>();
            msgs.add(msg.getBytes());
            ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMcAddr().getAddress(),
                    peerArgs.getAddressList().getMcAddr().getPort(), msgs);
        }

        System.out.println("Deleted previous file");

        FileMetadata fileMetadata = new FileMetadata(file.getPath(), fileId, repDgr);
        peer.getPeerMetadata().addHostingEntry(fileMetadata);

        // message initialization
        for (ConcurrentHashMap.Entry<Integer, byte[]> chunk : chunks.entrySet()) {
            PutChunk backupMsg = new PutChunk(peer.getPeerArgs().getVersion(), peer.getPeerArgs().getPeerId(), fileId,
                    chunk.getKey(), repDgr, chunk.getValue());
            messages.add(backupMsg.getBytes());
        }

        execute();
    }

    public void backupChunk(String fileId, int chunkNo) throws IOException {
        messages = new ArrayList<>();
        FileHandler fileHandler = new FileHandler(file);

        if (peer.getPeerMetadata().hasChunk(fileId,chunkNo)) {
            System.out.println("File already backed up, aborting...");
            return;
        }
        FileMetadata fileMetadata = new FileMetadata(file.getPath(), fileId, repDgr);
        peer.getPeerMetadata().addHostingEntry(fileMetadata);

        PutChunk backupMsg = new PutChunk(peer.getPeerArgs().getVersion(), peer.getPeerArgs().getPeerId(), fileId,
                chunkNo, repDgr, fileHandler.getChunkFileData());
        messages.add(backupMsg.getBytes());

        execute();
    }


    private void execute() {
        if (reps <= repsLimit) {
            ThreadHandler.startMulticastThread(peer.getPeerArgs().getAddressList().getMdbAddr().getAddress(),
                    peer.getPeerArgs().getAddressList().getMdbAddr().getPort(), messages);
            executor.schedule(this::verify, timeWait, TimeUnit.SECONDS);
            System.out.println("Sent message, waiting " + timeWait + " seconds...");
        } else {
            System.out.println("Reached resending limit of PUTCHUNK messages!");
            System.out.println("ERROR: Failed to  Back up file...");
        }
    }

    private void verify() {
        if (!peer.getPeerMetadata().verifyRepDgr(fileId, repDgr, numOfChunks)) {
            System.out.println("Did not get expected replication degree after " + timeWait + " seconds. Resending...");
            execute();
            reps++;
            timeWait *= 2;
        } else {
            System.out.println("Got expected replication degree!");
        }
    }
}
