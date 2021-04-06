package peer.metadata;

import java.util.ArrayList;
import java.util.List;

public class FileMetadata {
    private final String pathname;
    private final String id;
    private final int repDgr;
    private  List<ChunkMetadata> chunks = new ArrayList<>();

    public FileMetadata(String pathname, String id, int repDgr) {
        this.pathname = pathname;
        this.id = id;
        this.repDgr = repDgr;
    }

    public FileMetadata(String pathname, String id, int repDgr,ChunkMetadata chunkMetadata) {
        this.pathname = pathname;
        this.id = id;
        this.repDgr = repDgr;
        addChunk(chunkMetadata);
    }

    public void addChunk(int sizeKb,int chunkNo,int percRepDgr){
        addChunk(new ChunkMetadata(sizeKb,chunkNo,repDgr,percRepDgr));
    }

    public void addChunk(ChunkMetadata chunkMetadata){
        if(!chunks.contains(chunkMetadata)){
            chunks.add(chunkMetadata);
        }
    }

    public String getPathname() {
        return pathname;
    }

    public String getId() {
        return id;
    }

    public int getRepDgr() {
        return repDgr;
    }

    public List<ChunkMetadata> getChunks() {
        return chunks;
    }

    public void setChunks(List<ChunkMetadata> chunks) {
        this.chunks = chunks;
    }
}
