package channels;

import messages.PutChunk;
import messages.Stored;
import peer.Peer;
import peer.metadata.FileMetadata;
import utils.AddressList;
import utils.FileHandler;
import utils.ThreadHandler;
import utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    public void handle(DatagramPacket packet) {
        byte[] packetData = packet.getData();
        int bodyStartPos = getBodyStartPos(packetData);
        byte[] header = Arrays.copyOfRange(packetData, 0, bodyStartPos - 4);
        byte[] body = Arrays.copyOfRange(packetData, bodyStartPos, packet.getLength());

        String rcvd = new String(header, 0, header.length);
        PutChunk rcvdMsg = new PutChunk(rcvd, body);

        if (!rcvdMsg.getSenderId().equals(peer.getPeerArgs().getPeerId())) {
            FileHandler.saveChunk(rcvdMsg, peer.getFileSystem());
            saveStateMetadata(rcvdMsg);

            //If parse correctly, send stored msg to MC channel
            new ScheduledThreadPoolExecutor(1).schedule(new ConfirmationSender(rcvdMsg), Utils.generateRandomDelay(), TimeUnit.MILLISECONDS);
        }
    }

    private void saveStateMetadata(PutChunk rcvdMsg){
        String path = FileHandler.getFilePath(peer.getFileSystem(),rcvdMsg);

        try {
            peer.getPeerStateMetadata().updateInfo(path,rcvdMsg.getFileId(),rcvdMsg.getReplicationDeg(), rcvdMsg.getChunkNo(), 0,rcvdMsg.getBody().length);
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
