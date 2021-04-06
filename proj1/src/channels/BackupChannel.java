package channels;

import messages.PutChunk;
import messages.Stored;
import peer.Peer;
import utils.AddressList;
import utils.FileHandler;
import utils.ThreadHandler;

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

            //If parse correctly, send stored msg to MC channel
            new ScheduledThreadPoolExecutor(1).schedule(new ConfirmationSender(rcvdMsg), generateRandom(), TimeUnit.MILLISECONDS);
        }
    }

    private int generateRandom() {
        return (int) (Math.random() * (400 + 1));
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
