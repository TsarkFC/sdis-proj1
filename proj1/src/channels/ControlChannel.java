package channels;

import messages.Delete;
import messages.GetChunk;
import messages.Message;
import messages.Stored;
import peer.Peer;
import protocol.RestoreProtocol;
import utils.AddressList;
import utils.FileHandler;

import java.io.IOException;
import java.net.DatagramPacket;

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
        switch (msgType) {
            case "STORED" -> handleBackup(msgString);
            case "DELETE" -> handleDelete(msgString);
            case "GETCHUNK" -> handleRestore(msgString);
            case "REMOVED" -> handleReclaim(msgString);
            default -> System.out.println("\nERROR NOT PARSING THAT MESSAGE " + msgType);
        }
    }

    public void handleBackup(String msgString) throws IOException {
        System.out.println("Control Channel received Stored Msg: " + msgString);
        Stored msg = new Stored(msgString);
        peer.getPeerMetadata().updateStoredInfo(msg.getFileId(), msg.getChunkNo(), msg.getSenderId());
    }

    public void handleDelete(String msgString) throws IOException {
        System.out.println("Control Channel received Delete Msg: " + msgString);
        Delete msg = new Delete(msgString);
        FileHandler.deleteFile(msg.getFileId(), peer.getFileSystem());
        peer.getPeerMetadata().deleteFile(msg.getFileId());
    }

    public void handleRestore(String msgString) {
        System.out.println("Control Channel received Restore Msg: " + msgString);
        GetChunk msg = new GetChunk(msgString);
        RestoreProtocol.handleGetChunkMsg(msg, peer);
    }

    public void handleReclaim(String msgString) throws IOException {
        System.out.println("Control Channel received Removed Msg: " + msgString);
        //A peer that has a local copy of the chunk shall update its local count of this chunk
        //If this count drops below the desired replication degree of that chunk, it shall initiate
        // the chunk backup subprotocol between 0.jpg and 400 ms
        //if during this delay, a peer receives a PUTCHUNK message for the same file chunk,
        // it should back off and restrain from starting yet another backup subprotocol for that file chunk.
        //BackupProtocol backupProtocol = new BackupProtocol()
        //Stored msg = new Stored(msgString);
        //peer.getPeerStoredMetadata().updateChunkInfo(msg.getFileId(), msg.getChunkNo(), msg.getSenderId());
    }
}
