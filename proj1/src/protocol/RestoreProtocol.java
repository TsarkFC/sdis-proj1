package protocol;

import filehandler.FileHandler;
import messages.Chunk;
import messages.ChunkEnhanced;
import messages.GetChunk;
import peer.Peer;
import peer.PeerArgs;
import peer.metadata.Metadata;
import utils.AddressList;
import utils.ThreadHandler;
import utils.Utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RestoreProtocol extends Protocol {
    int repsLimit = 5;
    int timeWait = 1;
    int reps = 1;
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public RestoreProtocol(String path, Peer peer) {
        super(path, peer);
    }

    @Override
    public void initialize() {
        System.out.println("[RESTORE] Initializing Restore protocol");
        List<byte[]> messages = new ArrayList<>();
        Metadata metadata = peer.getMetadata();
        String fileId = metadata.getFileIdFromPath(path);
        PeerArgs peerArgs = peer.getArgs();
        peer.resetChunksReceived();

        if (!metadata.hasFile(fileId)) {
            System.out.println("[RESTORE] Peer has not hosted BACKUP to file");
            return;
        }

        peer.addRestoreEntry(fileId);
        int chunksNo = FileHandler.getNumberOfChunks(metadata.getFileSize(fileId));

        for (int i = 0; i < chunksNo; i++) {
            GetChunk getChunk = new GetChunk(peerArgs.getVersion(), peerArgs.getPeerId(), fileId, i);
            messages.add(getChunk.getBytes());
        }

        execute(messages, fileId);
    }

    private void execute(List<byte[]> messages, String fileId) {
        if (reps <= repsLimit) {
            AddressList addrList = peer.getArgs().getAddressList();
            ThreadHandler.startMulticastThread(addrList.getMcAddr().getAddress(), addrList.getMcAddr().getPort(), messages);
            executor.schedule(() -> verify(messages, fileId), timeWait, TimeUnit.SECONDS);
            System.out.println("[RESTORE] Sent message, waiting " + timeWait + " seconds...");
        } else {
            System.out.println("[RESTORE] Reached resending limit of PUTCHUNK messages!");
        }
    }

    private void verify(List<byte[]> messages, String fileId) {
        if (peer.hasRestoreEntry(fileId)) {
            System.out.println("[RESTORE] Did not complete after " + timeWait + " seconds. Resending...");
            reps++;
            timeWait *= 2;
            execute(messages, fileId);
        }
    }

    public static void handleGetChunkMsg(GetChunk rcvdMsg, Peer peer) {
        new ScheduledThreadPoolExecutor(1).
                schedule(() -> sendChunk(rcvdMsg, peer), Utils.generateRandomDelay("[RESTORE] Send Chunk msg after "), TimeUnit.MILLISECONDS);
    }

    public static void sendChunk(GetChunk rcvdMsg, Peer peer) {
        byte[] chunk = FileHandler.getChunk(rcvdMsg, peer.getFileSystem());
        if (chunk == null) {
            System.out.println("");
            return;
        }

        List<byte[]> msgs = new ArrayList<>();
        ServerSocket socket = startTcpServer();

        if (peer.getArgs().getVersion() == 1.0) {
            Chunk msg = new Chunk(rcvdMsg.getVersion(), peer.getArgs().getPeerId(), rcvdMsg.getFileId(),
                    rcvdMsg.getChunkNo(), chunk);
            msgs.add(msg.getBytes());
        } else {
            if (socket == null) {
                System.out.println("[RESTORE] could not start tcp server socket, aborting...");
                return;
            }
            int portNumber = socket.getLocalPort();
            ChunkEnhanced msg = new ChunkEnhanced(rcvdMsg.getVersion(), peer.getArgs().getPeerId(), rcvdMsg.getFileId(),
                    rcvdMsg.getChunkNo(), portNumber);
            msgs.add(msg.getBytes());
        }

        String chunkId = rcvdMsg.getFileId() + "-" + rcvdMsg.getChunkNo();
        if (peer.hasReceivedChunk(chunkId)) return;

        AddressList addrList = peer.getArgs().getAddressList();
        ThreadHandler.startMulticastThread(addrList.getMdrAddr().getAddress(), addrList.getMdrAddr().getPort(), msgs);

        if (peer.getArgs().getVersion() != 1.0) handleRestoreTcp(socket, chunk);
    }

    private static ServerSocket startTcpServer() {
        ServerSocket socket;
        try {
            socket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return socket;
    }

    private static void handleRestoreTcp(ServerSocket socket, byte[] chunk) {
        try {
            Socket clientSocket = socket.accept();
            BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());

            out.write(chunk);
            out.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
