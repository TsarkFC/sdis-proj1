package peer.metadata;

import java.util.Scanner;

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

    public String getString(){
        return String.format("%d,%d,%d,%d",sizeKb,id,repDgr,perceivedRepDgr);
    }


    public static ChunkMetadata readFile(String chunkStr){
        Scanner chunkScanner = new Scanner(chunkStr);
        chunkScanner.useDelimiter(",");
        int sizeKb = Integer.parseInt(chunkScanner.next());
        int id = Integer.parseInt(chunkScanner.next());
        int repDgr = Integer.parseInt(chunkScanner.next());
        int pRD = Integer.parseInt(chunkScanner.next());
        return new ChunkMetadata(sizeKb,id,repDgr,pRD);
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
