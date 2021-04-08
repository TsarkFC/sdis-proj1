package channels;

import messages.PutChunk;
import messages.Stored;
import peer.Peer;
import protocol.BackupProtocolInitiator;
import utils.AddressList;
import utils.FileHandler;
import utils.ThreadHandler;
import utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackupChannel extends Channel {

    public BackupChannel(AddressList addressList, Peer peer) {
        super(addressList, peer);
        super.currentAddr = addressList.getMdbAddr();
    }

    @Override
    public void handle(DatagramPacket packet) throws IOException {
        byte[] packetData = packet.getData();
        int bodyStartPos = getBodyStartPos(packetData);
        byte[] header = Arrays.copyOfRange(packetData, 0, bodyStartPos - 4);
        byte[] body = Arrays.copyOfRange(packetData, bodyStartPos, packet.getLength());

        String rcvd = new String(header, 0, header.length);
        PutChunk rcvdMsg = new PutChunk(rcvd, body);



        if (shouldSaveFile(rcvdMsg)) {
            System.out.println("Backing up file " + rcvdMsg.getFileId() + "-" + rcvdMsg.getChunkNo());
            System.out.println("\tFrom " + rcvdMsg.getSenderId());
            preventReclaim(rcvdMsg);
            FileHandler.saveChunk(rcvdMsg, peer.getFileSystem());
            saveStateMetadata(rcvdMsg);
            //If parse correctly, send stored msg to MC channel
            new ScheduledThreadPoolExecutor(1).schedule(new ConfirmationSender(rcvdMsg),
                    Utils.generateRandomDelay(), TimeUnit.MILLISECONDS);
        }
    }

    private boolean shouldSaveFile(PutChunk rcvdMsg){
        boolean sameSenderPeer = rcvdMsg.getSenderId().equals(peer.getPeerArgs().getPeerId());
        boolean hasSpace = peer.getPeerMetadata().hasSpace(rcvdMsg.getBody().length/1000.0);
        boolean isOriginalFileSender =peer.getPeerMetadata().hasFile(rcvdMsg.getFileId());
        double totalSpace = peer.getPeerMetadata().getStoredChunksMetadata().getStoredSize() + rcvdMsg.getBody().length/1000.0;
        System.out.println("Max space is: " + peer.getPeerMetadata().getMaxSpace() + " and total is " + totalSpace);
        System.out.println("New file size:" + rcvdMsg.getBody().length/1000.0);

        return !sameSenderPeer && hasSpace && !isOriginalFileSender;
    }

    private void saveStateMetadata(PutChunk rcvdMsg) throws IOException {
        peer.getPeerMetadata().updateStoredInfo(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), rcvdMsg.getReplicationDeg(),
                rcvdMsg.getBody().length/1000.0, peer.getPeerArgs().getPeerId());
    }

    private void preventReclaim(PutChunk rcvdMsg){
        BackupProtocolInitiator backupProtocolInitiator = peer.getChannelCoordinator().getBackupInitiator();
        if (backupProtocolInitiator!=null){
            backupProtocolInitiator.setReceivedPutChunk(rcvdMsg.getFileId(),rcvdMsg.getChunkNo());
        }

    }


    private class ConfirmationSender implements Runnable {
        PutChunk rcvdMsg;

        public ConfirmationSender(PutChunk rcvdMsg) {
            this.rcvdMsg = rcvdMsg;
        }

        public void run() {
            Stored confMsg = new Stored(rcvdMsg.getVersion(), peer.getPeerArgs().getPeerId(), rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
            sendConfirmationMc(confMsg.getBytes());
        }

        private void sendConfirmationMc(byte[] msg) {
            List<byte[]> msgs = new ArrayList<>();
            msgs.add(msg);
            ThreadHandler.startMulticastThread(addrList.getMcAddr().getAddress(), addrList.getMcAddr().getPort(), msgs);
        }
    }
}
