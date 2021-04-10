package peer.metadata;

import messages.Delete;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileMetadata implements Serializable {
    private final String pathname;
    private final String id;
    private final int repDgr;
    private final int size;
    private boolean deleted = false;

    /**
     * Maps chunk no to peer Ids that store the chunk
     */
    private Map<Integer, Set<Integer>> chunksData = new ConcurrentHashMap<>();

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

    public void removeID(int peersId){
        for (Set<Integer> peerIds : chunksData.values()){
            if(peerIds != null){
                if (peerIds.contains(peersId)){
                    peerIds.remove(peersId);
                }
            }
        }
    }

    public boolean peerHasChunk(int peerId){
        for (Set<Integer> peerIds : chunksData.values()){
            if(peerIds.contains(peerId)) return true;
        }
        return false;
    }

    public boolean deletedAllChunksAllPeers(){
        for (Set<Integer> peerIds : chunksData.values()){
            if(peerIds.size()!=0) return false;
        }
        return true;
    }

    public int getNumberPeersStoreChunk(int chunkId){
        Set<Integer> peersIds = chunksData.get(chunkId);
        if (peersIds != null){
            peersIds.remove(chunkId);
            return peersIds.size();
        }else return -1;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
