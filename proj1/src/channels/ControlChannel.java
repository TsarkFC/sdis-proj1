package channels;

import messages.*;
import peer.Peer;
import protocol.RestoreProtocol;
import utils.AddressList;
import utils.FileHandler;
import utils.ThreadHandler;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static utils.FileHandler.restoreChunk;

public class ControlChannel extends Channel {

    public ControlChannel(AddressList addressList, Peer peer) {
        super(addressList, peer);
        super.currentAddr = addressList.getMcAddr();
    }

    @Override
    public void handle(DatagramPacket packet) throws IOException {
        String rcvd = new String(packet.getData(), 0, packet.getLength());
        parseMsg(rcvd);
    }

    public void parseMsg(String msgString) throws IOException {
        String msgType = Message.getTypeStatic(msgString);
        if (msgType.equals("STORED")) handleBackup(msgString);
        else if (msgType.equals("DELETE")) handleDelete(msgString);
        else if (msgType.equals("GETCHUNK")) handleRestore(msgString);
        else System.out.println("\nERROR NOT PARSING THAT MESSAGE " + msgType);
    }

    public void handleBackup(String msgString) throws IOException {
        System.out.println("Control Channel received Stored Msg: " + msgString);
        Stored msg = new Stored(msgString);
        peer.getPeerMetadata().updateChunkInfo(msg.getFileId(), msg.getChunkNo(), msg.getSenderId());
    }

    public void handleDelete(String msgString) {
        System.out.println("Control Channel received Delete Msg: " + msgString);
        Delete msg = new Delete(msgString);
        List<Integer> storedChunkNumbers = FileHandler.getChunkNoStored(msg.getFileId(), peer.getFileSystem());
        FileHandler.deleteFile(msg.getFileId(), peer.getFileSystem());
        peer.getPeerMetadata().deleteChunksFile(storedChunkNumbers, msg.getFileId());
    }

    public void handleRestore(String msgString) {
        System.out.println("Control Channel received Restore Msg: " + msgString);
        GetChunk msg = new GetChunk(msgString);
        RestoreProtocol.handleGetChunk(msg, peer);
    }
}
