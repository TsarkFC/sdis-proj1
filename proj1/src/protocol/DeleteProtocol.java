package protocol;

import messages.Delete;
import messages.Deleted;
import peer.Peer;
import peer.PeerArgs;
import peer.metadata.Metadata;
import utils.AddressList;
import utils.ThreadHandler;

import java.util.ArrayList;
import java.util.List;

public class DeleteProtocol extends Protocol {

    public DeleteProtocol(String path, Peer peer) {
        super(path, peer);
    }

    @Override
    public void initialize() {
        System.out.println("[DELETE] Initializing Delete protocol");
        Metadata metadata = peer.getMetadata();
        String fileId = metadata.getFileIdFromPath(path);

        //TODO O que é isto?
        if (!metadata.hasFile(fileId)) {
            System.out.println("Peer has not hosted BACKUP to file");
            return;
        }
        peer.getMetadata().getFileMetadata(fileId).setDeleted(true);
        if (peer.isVanillaVersion()) peer.getMetadata().deleteFile(fileId);
        sendDeleteMessages(peer, fileId);
    }

    //TODO maybe we should make this method not static
    public static void sendDeleteMessages(Peer peer, String fileId) {
        PeerArgs peerArgs = peer.getArgs();
        List<byte[]> messages = new ArrayList<>();
        Delete msg = new Delete(peerArgs.getVersion(), peerArgs.getPeerId(), fileId);
        messages.add(msg.getBytes());
        ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMcAddr().getAddress(),
                peerArgs.getAddressList().getMcAddr().getPort(), messages);
    }

    public static void sendDeletedMessage(Peer peer, Delete deleteMsg) {
        AddressList addrList = peer.getArgs().getAddressList();
        if (!peer.isVanillaVersion()) {
            Deleted msg = new Deleted(deleteMsg.getVersion(), peer.getArgs().getPeerId(), deleteMsg.getFileId());
            List<byte[]> msgs = new ArrayList<>();
            msgs.add(msg.getBytes());
            ThreadHandler.startMulticastThread(addrList.getMcAddr().getAddress(), addrList.getMcAddr().getPort(), msgs);
        }
    }

}
