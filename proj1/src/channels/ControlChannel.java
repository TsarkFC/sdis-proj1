package channels;

import messages.*;
import peer.Peer;
import peer.metadata.ChunkMetadata;
import peer.metadata.FileMetadata;
import peer.metadata.StoredChunksMetadata;
import protocol.BackupProtocolInitiator;
import protocol.DeleteProtocol;
import protocol.RestoreProtocol;
import utils.AddressList;
import filehandler.FileHandler;
import utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
            case "DELETED" -> handleDeleted(msgString);
            case "STARTING" -> handleStart(msgString);
            default -> System.out.println("\nERROR NOT PARSING THAT MESSAGE " + msgType);
        }
    }

    public void handleBackup(String msgString) throws IOException {
        System.out.println("Control Channel received Stored Msg: " + msgString.substring(0, msgString.length() - 4));
        Stored msg = new Stored(msgString);
        peer.getMetadata().updateStoredInfo(msg.getFileId(), msg.getChunkNo(), msg.getSenderId());
        //System.out.println("PERCEIVED CONTROL CHANNEL: " +peer.getMetadata().getStoredChunksMetadata().getStoredCount(msg.getFileId(), msg.getChunkNo()));
    }

    public void handleDelete(String msgString) {
        Delete msg = new Delete(msgString);
        if(!msg.samePeerAndSender(peer)) {
            System.out.println("Control Channel received Delete Msg: " + msgString.substring(0, msgString.length() - 4));
            if(FileHandler.deleteFile(msg.getFileId(), peer.getFileSystem())){
                peer.getMetadata().deleteFile(msg.getFileId());
                DeleteProtocol.sendDeletedMessage(peer, msg);
            }
        }
    }

    public void handleDeleted(String msgString){
        Deleted msg = new Deleted(msgString);
        if(!msg.samePeerAndSender(peer) && !peer.isVanillaVersion()){
            System.out.println("Control Channel received DELETED Msg: " + msgString.substring(0, msgString.length() - 4));
            FileMetadata fileMetadata = peer.getMetadata().getHostingFileInfo().get(msg.getFileId());
            fileMetadata.removeID(msg.getSenderId());
            peer.getMetadata().writeMetadata();
            if (fileMetadata.deletedAllChunksAllPeers()){
                System.out.println("Successfully removed all chunks from all peers of file " + msg.getFileId());
                peer.getMetadata().deleteFileHosting(msg.getFileId(),peer);

            }
        }
    }

    public void handleStart(String msgString){
        Starting msg = new Starting(msgString);
        if(!msg.samePeerAndSender(peer) && !peer.isVanillaVersion()){
            System.out.println("Control Channel received STARTING Msg: " + msgString.substring(0, msgString.length() - 4));
            //Nao tem file id ver naquela dos stored chunks
            //TODO enviar apenas para peer msg.getPeerId()
            List<FileMetadata> almostDeletedFiles = peer.getMetadata().getAlmostDeletedFiles();
            System.out.println("Almost deleted files Size: " + almostDeletedFiles.size());
            for (FileMetadata almostDeletedFile:  almostDeletedFiles) {
                System.out.println("Sending delete message of file " + almostDeletedFile.getId());
                DeleteProtocol.sendDeleteMessages(peer,almostDeletedFile.getId());
            }
        }

    }

    public void handleRestore(String msgString) {
        System.out.println("Control Channel received Restore Msg: " + msgString.substring(0, msgString.length() - 4));
        GetChunk msg = new GetChunk(msgString);
        peer.resetChunksReceived();
        RestoreProtocol.handleGetChunkMsg(msg, peer);
    }

    public void handleReclaim(String msgString) {
        System.out.println("Control Channel received Removed Msg: " + msgString.substring(0, msgString.length() - 4));
        Removed removed = new Removed(msgString);
        //System.out.println(removed.getMsgType() + " " + removed.getFileId() + " " + removed.getChunkNo());
        //A peer that has a local copy of the chunk shall update its local count of this chunk
        //1- Check if chunk is stored
        peer.getMetadata().printState();
        StoredChunksMetadata storageMetadata = peer.getMetadata().getStoredChunksMetadata();
        int peerId = peer.getPeerArgs().getPeerId();
        if(storageMetadata.chunkIsStored(removed.getFileId(), removed.getChunkNo()) && !removed.samePeerAndSender(peerId)){
            //2- Update local count of its chunk
            ChunkMetadata chunkMetadata = storageMetadata.getChunk(removed.getFileId(), removed.getChunkNo());
            chunkMetadata.removePeer(removed.getSenderId());
            peer.getMetadata().printState();

            //If this count drops below the desired replication degree of that chunk, it shall initiate
            // the chunk backup subProtocol between 0 and 400 ms
            if(chunkMetadata.getPerceivedRepDgr() < chunkMetadata.getRepDgr()){
                BackupProtocolInitiator backupProtocolInitiator = new BackupProtocolInitiator(removed,chunkMetadata,peer);
                peer.getChannelCoordinator().setBackupInitiator(backupProtocolInitiator);
                new ScheduledThreadPoolExecutor(1).schedule(backupProtocolInitiator,
                        Utils.generateRandomDelay(), TimeUnit.MILLISECONDS);
            }
        }
    }
}
