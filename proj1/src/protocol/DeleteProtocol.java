package protocol;

import messages.handlers.DeleteHandler;
import peer.Peer;
import peer.metadata.Metadata;
import utils.ThreadHandler;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DeleteProtocol extends Protocol {
    int repsLimit = 3;
    int reps = 1;
    int timeWait = 1;
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public DeleteProtocol(String path, Peer peer) {
        super(path, peer);
    }

    @Override
    public void initialize() {
        System.out.println("[DELETE] Initializing Delete protocol");
        Metadata metadata = peer.getMetadata();
        String fileId = metadata.getFileIdFromPath(path);

        if (!metadata.hasFile(fileId)) {
            System.out.println("Peer has not hosted BACKUP to file");
            return;
        }
        peer.getMetadata().getFileMetadata(fileId).setDeleted(true);
        if (peer.isVanillaVersion()) peer.getMetadata().deleteFile(fileId);

        execute(fileId);
    }

    private void execute(String fileId) {
        if (reps <= repsLimit) {
            new DeleteHandler().sendDeleteMessages(peer, fileId);
            executor.schedule(() -> {
                reps++;
                execute(fileId);
            }, timeWait, TimeUnit.SECONDS);
            System.out.println("[DELETE] Sent message, waiting " + timeWait + " seconds...");
        }
    }
}
