package peer.metadata;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ChunkMetadata implements Serializable {
    private final int sizeKb;
    private final String id;
    private final int repDgr;
    private final Set<Integer> peerIds;

    public ChunkMetadata(int sizeKb, String id, int repDgr, Set<Integer> peerIds) {
        this.sizeKb = sizeKb;
        this.id = id;
        this.repDgr = repDgr;
        this.peerIds = peerIds;
    }

    public ChunkMetadata() {
        sizeKb = 0;
        id = "";
        repDgr = 0;
        peerIds = new HashSet<>();
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

    public Set<Integer> getPeerIds() {
        return peerIds;
    }

    public boolean biggerThanDesiredRep() {
        return getPerceivedRepDgr() > getRepDgr();
    }

    public void addPeer(Integer peerId) {
        peerIds.add(peerId);
    }

    public void removePeer(Integer peerId) {
        peerIds.remove(peerId);
    }


}
