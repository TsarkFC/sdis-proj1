package peer.metadata;

import java.io.Serializable;
import java.util.*;

public class FileMetadata implements Serializable {
    private final String pathname;
    private final String id;
    private final int repDgr;
    private Map<Integer, List<Integer>> chunksData = new HashMap<>();

    public FileMetadata(String pathname, String id, int repDgr) {
        this.pathname = pathname;
        this.id = id;
        this.repDgr = repDgr;
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

    public Map<Integer, List<Integer>> getChunksData() {
        return chunksData;
    }

    public void addChunk(Integer chunkId, Integer peerId) {
        List<Integer> peersIds = chunksData.get(chunkId);
        if (peersIds != null) {
            peersIds.add(peerId);
        } else {
            peersIds = new ArrayList<>();
            peersIds.add(peerId);
            chunksData.put(chunkId, peersIds);
        }
    }
}
