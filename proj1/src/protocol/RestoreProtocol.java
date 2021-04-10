package protocol;

import messages.Chunk;
import messages.ChunkEnhanced;
import messages.GetChunk;
import messages.MsgWithChunk;
import peer.Peer;
import peer.PeerArgs;
import filehandler.FileHandler;
import peer.metadata.Metadata;
import utils.AddressList;
import utils.ThreadHandler;
import utils.Utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static filehandler.FileHandler.CHUNK_SIZE;


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
        PeerArgs peerArgs = peer.getArgs();
        peer.resetChunksReceived();

        if (!metadata.hasFile(fileId)) {
            System.out.println("[RESTORE] Peer has not hosted BACKUP to file");
            return;
        }
        chunksNo = FileHandler.getNumberOfChunks(metadata.getFileSize(fileId));

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
        System.out.println("TCP SERVER CREATED");
        System.out.println("SERVER PORT NUMBER: " + socket.getLocalPort());
        return socket;
    }

    private static void handleRestoreTcp(ServerSocket socket, byte[] chunk) {
        try {
            System.out.println("HANDLE RESTORE TCP");
            Socket clientSocket = socket.accept();
            BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());

            System.out.println("REQUEST ACCEPTED!");
            out.write(chunk);
            out.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void handleChunkEnhancedMsg(ChunkEnhanced rcvdMsg) {
        if (complete) {
            return;
        }

        int portNumber = rcvdMsg.getPortNumber();
        System.out.println("CLIENT PORT NUMBER: " + portNumber);

        try (Socket socket = new Socket("localhost", portNumber);
             BufferedInputStream in = new BufferedInputStream(socket.getInputStream())) {

            byte[] chunk = new byte[CHUNK_SIZE];
            int readTest = in.readNBytes(chunk, 0, CHUNK_SIZE);
            in.close();
            System.out.println("READ FROM TCP: " + readTest);
            socket.close();

            chunksMap.put(rcvdMsg.getChunkNo(), chunk);

            if (chunksMap.size() >= chunksNo) {
                Path restoreFilePath = Paths.get(path);
                String filename = peer.getRestoreDir() + "/" + restoreFilePath.getFileName();
                FileHandler.restoreFile(filename, chunksMap);
                complete = true;
                System.out.println("[RESTORE] completed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
