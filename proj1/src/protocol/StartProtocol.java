package protocol;

import messages.Delete;
import messages.Deleted;
import messages.Starting;
import peer.Peer;
import peer.PeerArgs;
import peer.metadata.Metadata;
import utils.AddressList;
import utils.ThreadHandler;

import java.util.ArrayList;
import java.util.List;

public class StartProtocol{
    private Peer peer;

    public StartProtocol(Peer peer) {
        this.peer = peer;
    }

    public void sendStartingMessage() {
        if(!peer.isVanillaVersion()){
            AddressList addrList = peer.getPeerArgs().getAddressList();
            Starting msg = new Starting(peer.getPeerArgs().getVersion(),peer.getPeerArgs().getPeerId());
            List<byte[]> msgs = new ArrayList<>();
            msgs.add(msg.getBytes());
            ThreadHandler.startMulticastThread(addrList.getMcAddr().getAddress(), addrList.getMcAddr().getPort(), msgs);
        }

    }
}
