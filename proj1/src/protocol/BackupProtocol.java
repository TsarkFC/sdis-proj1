package protocol;

import messages.PutChunk;
import peer.Peer;
import utils.FileHandler;
import utils.ThreadHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackupProtocol extends Protocol {
    final int repDegree;
    int repsLimit = 5;
    List<byte[]> messages;
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    int storedExpected;
    int timeWait = 1;
    int reps = 1;
    String fileId;

    public BackupProtocol(File file, Peer peer, int repDegree) {
        super(file, peer);
        this.repDegree = repDegree;
    }

    @Override
    public void initialize() throws IOException {
        messages = new ArrayList<>();
        FileHandler fileHandler = new FileHandler(file);
        List<byte[]> chunks = fileHandler.splitFile();
        fileId = fileHandler.createFileId();
        storedExpected = chunks.size() * repDegree;

        // message initialization
        int chunkNo = 0;
        for (byte[] chunk : chunks) {
            PutChunk backupMsg = new PutChunk(peer.getPeerArgs().getVersion(), peer.getPeerArgs().getPeerId(), fileId, chunkNo, repDegree, chunk);
            byte[] msg = backupMsg.getBytes();
            messages.add(msg);
            chunkNo++;
        }

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
        }
    }

    private void verify() {
        if (storedExpected > peer.getPeerMetadata().getFileStoredCount(fileId)) {
            System.out.println("Did not get expected replication degree after " + timeWait + " seconds. Resending...");
            execute();
            reps++;
            timeWait *= 2;
        } else {
            System.out.println("Got expected replication degree!");
        }
    }
}
