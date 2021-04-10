package peer.metadata;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileMetadata implements Serializable {
    private final String pathname;
    private final String id;
    private final int repDgr;
    private final int size;
    private ConcurrentHashMap<Integer, Set<Integer>> chunksData = new ConcurrentHashMap<>();

    public FileMetadata(String pathname, String id, int repDgr, int size) {
        this.pathname = pathname;
        this.id = id;
        this.repDgr = repDgr;
        this.size = size;
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

    public Map<Integer, Set<Integer>> getChunksData() {
        return chunksData;
    }

    public int getSize() {
        return size;
    }

    public void addChunk(Integer chunkId, Integer peerId) {
        Set<Integer> peersIds = chunksData.get(chunkId);
        if (peersIds != null) {
            peersIds.add(peerId);
        } else {
            peersIds = new HashSet<>();
            peersIds.add(peerId);
            chunksData.put(chunkId, peersIds);
        }
    }
}
