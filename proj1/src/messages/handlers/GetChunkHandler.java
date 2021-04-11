package messages.handlers;

import filehandler.FileHandler;
import messages.Chunk;
import messages.ChunkEnhanced;
import messages.GetChunk;
import peer.Peer;
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

public class GetChunkHandler {
    public void handleGetChunkMsg(GetChunk rcvdMsg, Peer peer) {
        new ScheduledThreadPoolExecutor(1).
                schedule(() -> sendChunk(rcvdMsg, peer), Utils.generateRandomDelay("[RESTORE] Send Chunk msg after "), TimeUnit.MILLISECONDS);
    }

    private void sendChunk(GetChunk rcvdMsg, Peer peer) {
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

    private ServerSocket startTcpServer() {
        ServerSocket socket;
        try {
            socket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return socket;
    }

    private void handleRestoreTcp(ServerSocket socket, byte[] chunk) {
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
