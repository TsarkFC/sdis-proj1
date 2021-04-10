package channels;

import filehandler.FileHandler;
import messages.Chunk;
import messages.ChunkEnhanced;
import peer.Peer;
import protocol.RestoreProtocol;
import utils.AddressList;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static filehandler.FileHandler.CHUNK_SIZE;

public class RestoreChannel extends Channel{

    public RestoreChannel(AddressList addressList, Peer peer) {
        super(addressList, peer);
        super.currentAddr = addressList.getMdrAddr();
    }

    @Override
    public void handle(DatagramPacket packet) {
        byte[] packetData = packet.getData();
        int bodyStartPos = getBodyStartPos(packetData);
        byte[] header = Arrays.copyOfRange(packetData, 0, bodyStartPos - 4);
        byte[] body = Arrays.copyOfRange(packetData, bodyStartPos, packet.getLength());

        String headerString = new String(header);
        System.out.println("[RECEIVED MESSAGE MDR] " + headerString);

        if (peer.getArgs().getVersion() == 1.0) {
            Chunk msg = new Chunk(headerString, body);
            String chunkId = msg.getFileId() + "-" + msg.getChunkNo();
            peer.addChunkReceived(chunkId);
            handleChunkMsg(msg);
        }
        else {
            ChunkEnhanced msg = new ChunkEnhanced(headerString);
            String chunkId = msg.getFileId() + "-" + msg.getChunkNo();
            peer.addChunkReceived(chunkId);
            handleChunkEnhancedMsg(msg);
        }

    }

    public void handleChunkMsg(Chunk rcvdMsg) {
        peer.addChunk(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), rcvdMsg.getBody());
    }

    public void handleChunkEnhancedMsg(ChunkEnhanced rcvdMsg) {

        if (!peer.hasRestoreEntry(rcvdMsg.getFileId())) return;

        int portNumber = rcvdMsg.getPortNumber();
        System.out.println("[TCP] Client port number: " + portNumber);

        try (Socket socket = new Socket("localhost", portNumber);
             BufferedInputStream in = new BufferedInputStream(socket.getInputStream())) {

            byte[] chunk = new byte[CHUNK_SIZE];
            int readTest = in.readNBytes(chunk, 0, CHUNK_SIZE);
            in.close();
            System.out.println("[TCP] Read from TCP: " + readTest);
            socket.close();

            peer.addChunk(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), chunk);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
