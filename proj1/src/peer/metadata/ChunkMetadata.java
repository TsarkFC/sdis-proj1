package peer.metadata;

import java.io.Serializable;
import java.util.List;

public class ChunkMetadata implements Serializable {
    private final int sizeKb;
    private final String id;
    private final int repDgr;
    private List<Integer> peerIds;

    public ChunkMetadata(int sizeKb, String id, int repDgr, List<Integer> peerIds) {
        this.sizeKb = sizeKb;
        this.id = id;
        this.repDgr = repDgr;
        this.peerIds = peerIds;
    }

    public String getString() {
        return String.format("%d, %s, %d, %d", sizeKb, id, repDgr, peerIds.size());
    }

    public int getSizeKb() {
        return sizeKb;
    }

    public String getId() {
        return id;
    }

    public int getRepDgr() {
        return repDgr;
    }

    public int getPerceivedRepDgr() {
        return peerIds.size();
    }

    public void addPeer(Integer peerId) {
        if (!peerIds.contains(peerId))
            peerIds.add(peerId);
    }

    public void removePeer(Integer peerId){
        if (peerIds.contains(peerId)){
            peerIds.remove(peerId);
        }
    }
}
