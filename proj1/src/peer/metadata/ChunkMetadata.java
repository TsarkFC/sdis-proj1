package peer.metadata;

public class ChunkMetadata {
    private int sizeKb;
    private int id;
    private int repDgr;
    private int perceivedRepDgr;

    public ChunkMetadata(int sizeKb, int id, int repDgr, int perceivedRepDgr) {
        this.sizeKb = sizeKb;
        this.id = id;
        this.repDgr = repDgr;
        this.perceivedRepDgr = perceivedRepDgr;
    }

    public int getSizeKb() {
        return sizeKb;
    }

    public int getId() {
        return id;
    }

    public int getRepDgr() {
        return repDgr;
    }

    public int getPerceivedRepDgr() {
        return perceivedRepDgr;
    }
}
