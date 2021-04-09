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

    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public BackupChannel(AddressList addressList, Peer peer) {
        super(addressList, peer);
        super.currentAddr = addressList.getMdbAddr();
    }

    @Override
    public void handle(DatagramPacket packet) {
        byte[] packetData = packet.getData();
        int bodyStartPos = getBodyStartPos(packetData);
        byte[] header = Arrays.copyOfRange(packetData, 0, bodyStartPos - 4);
        byte[] body = Arrays.copyOfRange(packetData, bodyStartPos, packet.getLength());

        String rcvd = new String(header, 0, header.length);
        PutChunk rcvdMsg = new PutChunk(rcvd, body);



        if (shouldSaveFile(rcvdMsg)) {
            //If parse correctly, send stored msg to MC channel
            executor.schedule(new ConfirmationSender(rcvdMsg),
                    Utils.generateRandomDelay(), TimeUnit.MILLISECONDS);
        }
    }

    private boolean shouldSaveFile(PutChunk rcvdMsg){
        boolean sameSenderPeer = rcvdMsg.getSenderId().equals(peer.getPeerArgs().getPeerId());
        boolean hasSpace = peer.getPeerMetadata().hasSpace(rcvdMsg.getBody().length/1000.0);
        boolean isOriginalFileSender =peer.getPeerMetadata().hasFile(rcvdMsg.getFileId());
        //double totalSpace = peer.getPeerMetadata().getStoredChunksMetadata().getStoredSize() + rcvdMsg.getBody().length/1000.0;
        return !sameSenderPeer && hasSpace && !isOriginalFileSender;
    }

    private void saveStateMetadata(PutChunk rcvdMsg)  {
        try {
            peer.getPeerMetadata().updateStoredInfo(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), rcvdMsg.getReplicationDeg(),
                    rcvdMsg.getBody().length/1000.0, peer.getPeerArgs().getPeerId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private class ConfirmationSender implements Runnable {
        PutChunk rcvdMsg;

        public ConfirmationSender(PutChunk rcvdMsg) {
            this.rcvdMsg = rcvdMsg;
        }

        public void run() {

            if (!alreadyReachedRepDgr(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(),rcvdMsg.getReplicationDeg())){
                System.out.println("Backing up file " + rcvdMsg.getFileId() + "-" + rcvdMsg.getChunkNo());
                System.out.println("\tFrom " + rcvdMsg.getSenderId());
                //TODO confirmar que este prevent reclaim deve ser aqui dentro e nao antes
                preventReclaim(rcvdMsg);
                FileHandler.saveChunk(rcvdMsg, peer.getFileSystem());
                saveStateMetadata(rcvdMsg);
                Stored confMsg = new Stored(rcvdMsg.getVersion(), peer.getPeerArgs().getPeerId(), rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
                sendConfirmationMc(confMsg.getBytes());
            }else{
                System.out.println("Not backing up because reached perceived rep degree");
            }
        }

        private void sendConfirmationMc(byte[] msg) {
            List<byte[]> msgs = new ArrayList<>();
            msgs.add(msg);
            ThreadHandler.startMulticastThread(addrList.getMcAddr().getAddress(), addrList.getMcAddr().getPort(), msgs);
        }

        private void preventReclaim(PutChunk rcvdMsg){
            BackupProtocolInitiator backupProtocolInitiator = peer.getChannelCoordinator().getBackupInitiator();
            if (backupProtocolInitiator!=null){
                backupProtocolInitiator.setReceivedPutChunk(rcvdMsg.getFileId(),rcvdMsg.getChunkNo());
            }
        }

        private boolean alreadyReachedRepDgr(String fileId,int chunkNo,int repDgr){
            int stored = peer.getPeerMetadata().getStoredChunksMetadata().getStoredCount(fileId,chunkNo);
            System.out.println("REP DGR: " + repDgr + " PERCEIVED =" + stored);
            if(stored >= repDgr) return true;
            return false;
        }
    }
}
