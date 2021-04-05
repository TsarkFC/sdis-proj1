package channels;

import messages.PutChunk;
import messages.Stored;
import peer.Peer;
import utils.AddressList;
import utils.FileHandler;
import utils.ThreadHandler;

import java.net.DatagramPacket;
import java.util.ArrayList;
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
        String recv = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Length: " + packet.getLength());
        System.out.println("Received message from MDB channel");
        PutChunk recvMsg = new PutChunk(recv);

        if (!recvMsg.getSenderId().equals(peer.getPeerArgs().getPeerId())) {
            FileHandler.saveChunk(recvMsg, peer.getFileSystem());

            //If parse correctly, send stored msg to MC channel
            new ScheduledThreadPoolExecutor(1).schedule(new ConfirmationSender(recvMsg), generateRandom(), TimeUnit.MILLISECONDS);
            Stored confMsg = new Stored(recvMsg.getVersion(), peer.getPeerArgs().getPeerId(), recvMsg.getFileId(), recvMsg.getChunkNo());
            sendConfirmationMc(confMsg.getBytes());
        }
    }

    public void sendConfirmationMc(byte[] msg) {
        List<byte[]> msgs = new ArrayList<>();
        msgs.add(msg);
        ThreadHandler.startMulticastThread(addrList.getMcAddr().getAddress(), addrList.getMcAddr().getPort(), msgs);
    }

    private int generateRandom() {
        return (int) (Math.random() * (400+1));
    }

    private class ConfirmationSender implements Runnable {
        PutChunk recvMsg;

        public ConfirmationSender(PutChunk recvMsg) {
            this.recvMsg = recvMsg;
        }

        public void run() {
            System.out.println("Sent confirmation message!");
            Stored confMsg = new Stored(recvMsg.getVersion(), peer.getPeerArgs().getPeerId(), recvMsg.getFileId(), recvMsg.getChunkNo());
            sendConfirmationMc(confMsg.getBytes());
        }
    }
}
